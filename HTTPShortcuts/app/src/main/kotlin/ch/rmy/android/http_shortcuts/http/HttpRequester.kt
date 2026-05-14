package ch.rmy.android.http_shortcuts.http

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import ch.rmy.android.framework.extensions.isSuccessfulOrRedirect
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.shouldIncludeInHistory
import ch.rmy.android.http_shortcuts.extensions.toCertificatePin
import ch.rmy.android.http_shortcuts.history.HistoryEvent
import ch.rmy.android.http_shortcuts.history.HistoryEventLogger
import ch.rmy.android.http_shortcuts.http.HttpHeaders.Companion.CONTENT_LENGTH
import ch.rmy.android.http_shortcuts.http.HttpHeaders.Companion.CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE_WITH_CHARSET
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.http_shortcuts.variables.ResolvedVariableValues
import ch.rmy.android.http_shortcuts.variables.Variables
import java.io.IOException
import java.io.InputStream
import java.net.UnknownHostException
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.CookieJar
import okhttp3.Response

class HttpRequester
@Inject
constructor(
    private val context: Context,
    private val httpClientFactory: HttpClientFactory,
    private val responseFileStorageFactory: ResponseFileStorageFactory,
    private val cookieManager: CookieManager,
    private val historyEventLogger: HistoryEventLogger,
    private val errorFormatter: ErrorFormatter,
) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    suspend fun executeShortcut(
        context: Context,
        shortcut: Shortcut,
        headers: List<RequestHeader>,
        parameters: List<RequestParameter>,
        storeDirectoryUri: Uri?,
        sessionId: String,
        variableValues: ResolvedVariableValues,
        fileUploadResult: FileUploadManager.Result? = null,
        useCookieJar: Boolean = false,
        certificatePins: List<CertificatePin>,
        validateRequestData: suspend (RequestData) -> Unit = {},
        progressTracker: ProgressTracker? = null,
    ): ShortcutResponse =
        withContext(Dispatchers.IO) {
            val responseFileStorage = responseFileStorageFactory.create(sessionId, storeDirectoryUri)
            val requestData = RequestData(
                url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variableValues).trim(),
                username = Variables.rawPlaceholdersToResolvedValues(shortcut.authUsername, variableValues),
                password = Variables.rawPlaceholdersToResolvedValues(shortcut.authPassword, variableValues),
                authToken = Variables.rawPlaceholdersToResolvedValues(shortcut.authToken, variableValues),
                body = if (shortcut.usesCustomBody()) Variables.rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues) else "",
                proxy = getProxyParams(shortcut, variableValues),
                contentType = determineContentType(shortcut),
            )

            validateRequestData(requestData)

            val cookieJar = if (useCookieJar) cookieManager.getCookieJar() else null

            try {
                makeRequest(
                    context = context,
                    shortcut = shortcut,
                    headers = headers,
                    parameters = parameters,
                    variablesValues = variableValues,
                    requestData = requestData,
                    responseFileStorage = responseFileStorage,
                    fileUploadResult = fileUploadResult,
                    cookieJar = cookieJar,
                    certificatePins = certificatePins,
                    progressTracker = progressTracker,
                )
            } catch (e: UnknownHostException) {
                ensureActive()
                if (ServiceDiscoveryHelper.isDiscoverable(requestData.uri)) {
                    val newRequestData = try {
                        val newHost = ServiceDiscoveryHelper.discoverService(context, requestData.uri.host!!)
                        ensureActive()
                        requestData.copy(
                            url = requestData.uri
                                .buildUpon()
                                .encodedAuthority("${newHost.address}:${newHost.port}")
                                .build()
                                .toString(),
                        )
                    } catch (_: ServiceDiscoveryHelper.ServiceLookupTimeoutException) {
                        requestData
                    }
                    makeRequest(
                        context = context,
                        shortcut = shortcut,
                        headers = headers,
                        parameters = parameters,
                        variablesValues = variableValues,
                        requestData = newRequestData,
                        responseFileStorage = responseFileStorage,
                        fileUploadResult = fileUploadResult,
                        cookieJar = cookieJar,
                        certificatePins = certificatePins,
                        progressTracker = progressTracker,
                    )
                } else {
                    throw e
                }
            }
        }

    private fun getProxyParams(shortcut: Shortcut, variableValues: ResolvedVariableValues): ProxyParams? {
        val host = (shortcut.proxyHost ?: return null)
            .let {
                Variables.rawPlaceholdersToResolvedValues(it, variableValues)
            }
            .trim()

        val username = shortcut.proxyUsername
            ?.let {
                Variables.rawPlaceholdersToResolvedValues(it, variableValues)
            }
        val password = shortcut.proxyPassword
            ?.let {
                Variables.rawPlaceholdersToResolvedValues(it, variableValues)
            }

        return ProxyParams(
            type = shortcut.proxyType ?: return null,
            host = host,
            port = shortcut.proxyPort ?: return null,
            username = username.orEmpty(),
            password = password.orEmpty(),
        )
    }

    private suspend fun makeRequest(
        context: Context,
        shortcut: Shortcut,
        headers: List<RequestHeader>,
        parameters: List<RequestParameter>,
        variablesValues: ResolvedVariableValues,
        requestData: RequestData,
        responseFileStorage: ResponseFileStorage,
        fileUploadResult: FileUploadManager.Result? = null,
        cookieJar: CookieJar? = null,
        certificatePins: List<CertificatePin>,
        progressTracker: ProgressTracker?,
    ): ShortcutResponse =
        suspendCancellableCoroutine { continuation ->
            val useDigestAuth = shortcut.authenticationType == ShortcutAuthenticationType.DIGEST
            val client = httpClientFactory.getClient(
                context = context,
                username = requestData.username.takeIf { useDigestAuth },
                password = requestData.password.takeIf { useDigestAuth },
                followRedirects = shortcut.followRedirects,
                timeout = shortcut.timeout.toLong(),
                ipVersion = shortcut.ipVersion,
                proxy = requestData.proxy,
                cookieJar = cookieJar,
                certificatePins = certificatePins.map(CertificatePin::toCertificatePin),
                clientCertParams = shortcut.clientCertParams,
                hostVerificationConfig = shortcut.getSSLConfig(),
            )

            val request = buildRequest(shortcut.method.method, requestData.url) {
                if (!shortcut.keepConnectionOpen) {
                    header(HttpHeaders.CONNECTION, "close")
                }
                userAgent(UserAgentProvider.getUserAgent(context))
                if (shortcut.usesCustomBody()) {
                    contentType(requestData.contentType)
                    body(requestData.body)
                }
                if (shortcut.usesGenericFileBody()) {
                    fileUploadResult?.getFile(0)
                        ?.let { file ->
                            val (stream, length) = file.getInputStreamAndLength()
                            contentType(requestData.contentType ?: file.mimeType)
                            body(stream, length)
                        }
                }
                if (shortcut.usesRequestParameters()) {
                    contentType(requestData.contentType)
                    attachParameters(parameters, variablesValues, fileUploadResult)
                }
                headers.forEach { header ->
                    header(
                        Variables.rawPlaceholdersToResolvedValues(header.key, variablesValues),
                        Variables.rawPlaceholdersToResolvedValues(header.value, variablesValues),
                    )
                }
                when (shortcut.authenticationType) {
                    ShortcutAuthenticationType.BASIC -> {
                        basicAuth(requestData.username, requestData.password)
                    }
                    ShortcutAuthenticationType.BEARER -> {
                        bearerAuth(requestData.authToken)
                    }
                    ShortcutAuthenticationType.DIGEST,
                    null,
                    -> {
                        requestData.uri.userInfo
                            ?.takeUnlessEmpty()
                            ?.split(':')
                            ?.takeIf { it.size == 2 }
                            ?.let { (username, password) ->
                                basicAuth(username, password)
                            }
                    }
                }
                withProgressTracker(progressTracker)
            }

            if (shortcut.shouldIncludeInHistory()) {
                historyEventLogger.logEvent(
                    HistoryEvent.HttpRequestSent(
                        shortcutName = shortcut.name,
                        url = request.url.toString().toUri(),
                        method = request.method,
                        headers = request.headers.toMultimap(),
                    ),
                )
            }

            logInfo("Starting HTTP request")
            try {
                client
                    .newCall(request)
                    .apply {
                        continuation.invokeOnCancellation {
                            cancel()
                        }
                    }
                    .execute()
                    .use { okHttpResponse ->
                        logInfo(
                            "HTTP request completed (body content length=${
                                Formatter.formatShortFileSize(
                                    context,
                                    okHttpResponse.body.contentLength(),
                                )
                            })",
                        )
                        val contentFile = if (shortcut.usesResponseBody()) {
                            responseFileStorage.store(okHttpResponse)
                        } else {
                            null
                        }

                        val isSuccess = okHttpResponse.isSuccessfulOrRedirect()

                        if (shortcut.shouldIncludeInHistory()) {
                            historyEventLogger.logEvent(
                                HistoryEvent.HttpResponseReceived(
                                    shortcutName = shortcut.name,
                                    responseCode = okHttpResponse.code,
                                    headers = okHttpResponse.headers.toMultimap(),
                                    isSuccess = isSuccess,
                                ),
                            )
                        }

                        val shortcutResponse = prepareResponse(
                            url = requestData.url,
                            response = okHttpResponse,
                            contentFile = contentFile,
                            charsetOverride = shortcut.responseCharset,
                        )
                        if (isSuccess) {
                            continuation.resume(shortcutResponse)
                        } else {
                            continuation.resumeWithException(ErrorResponse(shortcutResponse))
                        }
                    }
            } catch (e: IOException) {
                if (shortcut.shouldIncludeInHistory()) {
                    historyEventLogger.logEvent(
                        HistoryEvent.NetworkError(
                            shortcutName = shortcut.name,
                            error = errorFormatter.getErrorMessage(e),
                        ),
                    )
                }
                throw e
            }
        }

    private fun RequestBuilder.attachParameters(
        parameters: List<RequestParameter>,
        variables: ResolvedVariableValues,
        fileUploadResult: FileUploadManager.Result?,
    ) {
        var fileIndex = -1
        parameters.forEach { parameter ->
            val parameterName = Variables.rawPlaceholdersToResolvedValues(parameter.key, variables)
            when (parameter.parameterType) {
                ParameterType.FILE,
                -> {
                    fileUploadResult?.let {
                        fileIndex++
                        if (parameter.fileUploadType == FileUploadType.FILE_PICKER_MULTI) {
                            fileUploadResult.getFiles(fileIndex)
                                .forEach { file ->
                                    val (stream, length) = file.getInputStreamAndLength()
                                    fileParameter(
                                        name = "$parameterName[]",
                                        fileName = parameter.fileUploadFileName?.takeUnlessEmpty() ?: file.fileName,
                                        type = file.mimeType,
                                        data = stream,
                                        length = length,
                                    )
                                }
                        } else {
                            fileUploadResult.getFile(fileIndex)
                                ?.let { file ->
                                    val (stream, length) = file.getInputStreamAndLength()
                                    fileParameter(
                                        name = parameterName,
                                        fileName = parameter.fileUploadFileName?.takeUnlessEmpty() ?: file.fileName,
                                        type = file.mimeType,
                                        data = stream,
                                        length = length,
                                    )
                                }
                        }
                    }
                }
                ParameterType.STRING -> {
                    parameter(
                        name = parameterName,
                        value = Variables.rawPlaceholdersToResolvedValues(parameter.value, variables),
                    )
                }
            }
        }
    }

    @SuppressLint("Recycle")
    private fun FileUploadManager.File.getInputStreamAndLength(): Pair<InputStream, Long?> {
        if (data.scheme == "data") {
            val value = data.schemeSpecificPart!!.drop(1)
            val bytes = value.toByteArray()
            return bytes.inputStream() to bytes.size.toLong()
        }
        return contentResolver.openInputStream(data)!! to FileUtil.getFileSize(contentResolver, data)
    }

    companion object {

        internal fun prepareResponse(url: String, response: Response, contentFile: DocumentFile?, charsetOverride: Charset?) =
            ShortcutResponse(
                url = url,
                headers = HttpHeaders.parse(response.headers),
                statusCode = response.code,
                contentFile = contentFile,
                timing = (response.receivedResponseAtMillis - response.sentRequestAtMillis).milliseconds,
                charsetOverride = charsetOverride,
            )

        internal fun determineContentType(shortcut: Shortcut): String? =
            when (shortcut.requestBodyType) {
                RequestBodyType.FORM_DATA -> FORM_MULTIPART_CONTENT_TYPE
                RequestBodyType.X_WWW_FORM_URLENCODE -> FORM_URLENCODE_CONTENT_TYPE_WITH_CHARSET
                else -> shortcut.contentType.takeUnlessEmpty()
            }

        internal fun Response.isStreaming(): Boolean =
            getHeaderValue(CONTENT_TYPE)?.takeWhile { it != ';' } == "text/event-stream"

        internal fun Response.isUnknownLength(): Boolean =
            getHeaderValue(CONTENT_LENGTH) == null

        private fun Response.getHeaderValue(name: String): String? =
            headers.lastOrNull { it.first.equals(name, ignoreCase = true) }?.second
    }
}
