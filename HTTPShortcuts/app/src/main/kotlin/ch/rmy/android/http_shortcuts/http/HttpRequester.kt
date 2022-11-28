package ch.rmy.android.http_shortcuts.http

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE_WITH_CHARSET
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.CookieJar
import okhttp3.Response
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpRequester
@Inject
constructor(
    private val context: Context,
    private val httpClientFactory: HttpClientFactory,
    private val responseFileStorageFactory: ResponseFileStorageFactory,
    private val cookieManager: CookieManager,
) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    suspend fun executeShortcut(
        context: Context,
        shortcut: ShortcutModel,
        sessionId: String,
        variableValues: Map<VariableId, String>,
        fileUploadResult: FileUploadManager.Result? = null,
        useCookieJar: Boolean = false,
    ): ShortcutResponse =
        withContext(Dispatchers.IO) {
            val responseFileStorage = responseFileStorageFactory.create(sessionId)
            val requestData = RequestData(
                url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variableValues).trim(),
                username = Variables.rawPlaceholdersToResolvedValues(shortcut.username, variableValues),
                password = Variables.rawPlaceholdersToResolvedValues(shortcut.password, variableValues),
                authToken = Variables.rawPlaceholdersToResolvedValues(shortcut.authToken, variableValues),
                body = Variables.rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues),
                proxy = getProxyParams(shortcut, variableValues),
            )

            val cookieJar = if (useCookieJar) cookieManager.getCookieJar() else null

            try {
                makeRequest(context, shortcut, variableValues, requestData, responseFileStorage, fileUploadResult, cookieJar)
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
                                .toString()
                        )
                    } catch (discoveryError: ServiceDiscoveryHelper.ServiceLookupTimeoutException) {
                        requestData
                    }
                    makeRequest(context, shortcut, variableValues, newRequestData, responseFileStorage, fileUploadResult, cookieJar)
                } else {
                    throw e
                }
            }
        }

    private fun getProxyParams(shortcut: ShortcutModel, variableValues: Map<VariableId, String>): ProxyParams? {
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
            type = shortcut.proxyType,
            host = host,
            port = shortcut.proxyPort ?: return null,
            username = username.orEmpty(),
            password = password.orEmpty(),
        )
    }

    private suspend fun makeRequest(
        context: Context,
        shortcut: ShortcutModel,
        variablesValues: Map<VariableId, String>,
        requestData: RequestData,
        responseFileStorage: ResponseFileStorage,
        fileUploadResult: FileUploadManager.Result? = null,
        cookieJar: CookieJar? = null,
    ): ShortcutResponse =
        suspendCancellableCoroutine { continuation ->
            val useDigestAuth = shortcut.authenticationType == ShortcutAuthenticationType.DIGEST
            val client = httpClientFactory.getClient(
                context = context,
                clientCertParams = shortcut.clientCertParams,
                acceptAllCertificates = shortcut.acceptAllCertificates,
                username = requestData.username.takeIf { useDigestAuth },
                password = requestData.password.takeIf { useDigestAuth },
                followRedirects = shortcut.followRedirects,
                timeout = shortcut.timeout.toLong(),
                proxy = requestData.proxy,
                cookieJar = cookieJar,
            )

            val request = RequestBuilder(shortcut.method, requestData.url)
                .header(HttpHeaders.CONNECTION, "close")
                .userAgent(UserAgentUtil.userAgent)
                .runIf(shortcut.usesCustomBody()) {
                    contentType(determineContentType(shortcut))
                        .body(requestData.body)
                }
                .runIf(shortcut.usesGenericFileBody() || shortcut.usesImageFileBody()) {
                    val file = fileUploadResult?.getFile(0)
                    runIfNotNull(file) {
                        contentType(determineContentType(shortcut) ?: it.mimeType)
                            .body(contentResolver.openInputStream(it.data)!!, length = it.fileSize)
                    }
                }
                .runIf(shortcut.usesRequestParameters()) {
                    contentType(determineContentType(shortcut))
                        .run {
                            attachParameters(this, shortcut, variablesValues, fileUploadResult)
                        }
                }
                .runFor(shortcut.headers) { header ->
                    header(
                        Variables.rawPlaceholdersToResolvedValues(header.key, variablesValues),
                        Variables.rawPlaceholdersToResolvedValues(header.value, variablesValues)
                    )
                }
                .runIf(shortcut.authenticationType == ShortcutAuthenticationType.BASIC) {
                    basicAuth(requestData.username, requestData.password)
                }
                .runIf(shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                    bearerAuth(requestData.authToken)
                }
                .build()

            responseFileStorage.clear()

            logInfo("Starting HTTP request")
            client
                .newCall(request)
                .apply {
                    continuation.invokeOnCancellation {
                        cancel()
                    }
                }
                .execute()
                .use { okHttpResponse ->
                    logInfo("HTTP request completed")
                    val contentFile = if (shortcut.usesResponseBody) {
                        responseFileStorage.store(okHttpResponse)
                    } else null
                    val shortcutResponse = prepareResponse(requestData.url, okHttpResponse, contentFile)
                    if (okHttpResponse.code in 200..399) {
                        continuation.resume(shortcutResponse)
                    } else {
                        continuation.resumeWithException(ErrorResponse(shortcutResponse))
                    }
                }
        }

    private fun attachParameters(
        requestBuilder: RequestBuilder,
        shortcut: ShortcutModel,
        variables: Map<VariableKey, String>,
        fileUploadResult: FileUploadManager.Result?,
    ): RequestBuilder {
        var fileIndex = -1
        return requestBuilder.runFor(shortcut.parameters) { parameter ->
            val parameterName = Variables.rawPlaceholdersToResolvedValues(parameter.key, variables)
            when (parameter.parameterType) {
                ParameterType.FILES -> {
                    runIfNotNull(fileUploadResult) {
                        fileIndex++
                        val files = it.getFiles(fileIndex)
                        runFor(files) { file ->
                            fileParameter(
                                name = "$parameterName[]",
                                fileName = parameter.fileName.ifEmpty { file.fileName },
                                type = file.mimeType,
                                data = contentResolver.openInputStream(file.data)!!,
                                length = file.fileSize,
                            )
                        }
                    }
                }
                ParameterType.IMAGE,
                ParameterType.FILE,
                -> {
                    runIfNotNull(fileUploadResult) {
                        fileIndex++
                        runIfNotNull(it.getFile(fileIndex)) { file ->
                            fileParameter(
                                name = parameterName,
                                fileName = parameter.fileName.ifEmpty { file.fileName },
                                type = file.mimeType,
                                data = contentResolver.openInputStream(file.data)!!,
                                length = file.fileSize,
                            )
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

    companion object {

        internal fun prepareResponse(url: String, response: Response, contentFile: Uri?) =
            ShortcutResponse(
                url = url,
                headers = HttpHeaders.parse(response.headers),
                statusCode = response.code,
                contentFile = contentFile,
                timing = response.receivedResponseAtMillis - response.sentRequestAtMillis,
            )

        internal fun determineContentType(shortcut: ShortcutModel): String? =
            when (shortcut.bodyType) {
                RequestBodyType.FORM_DATA -> FORM_MULTIPART_CONTENT_TYPE
                RequestBodyType.X_WWW_FORM_URLENCODE -> FORM_URLENCODE_CONTENT_TYPE_WITH_CHARSET
                else -> shortcut.contentType.takeUnlessEmpty()
            }
    }
}
