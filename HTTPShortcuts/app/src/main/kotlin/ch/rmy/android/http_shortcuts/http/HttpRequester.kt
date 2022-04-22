package ch.rmy.android.http_shortcuts.http

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.CookieJar
import okhttp3.Response
import java.net.UnknownHostException

class HttpRequester(private val contentResolver: ContentResolver) {

    fun executeShortcut(
        context: Context,
        shortcut: ShortcutModel,
        variableManager: VariableManager,
        responseFileStorage: ResponseFileStorage,
        fileUploadManager: FileUploadManager? = null,
        cookieJar: CookieJar? = null,
    ): Single<ShortcutResponse> =
        Single
            .fromCallable {
                val variables = variableManager.getVariableValuesByIds()

                RequestData(
                    url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variables).trim(),
                    username = Variables.rawPlaceholdersToResolvedValues(shortcut.username, variables),
                    password = Variables.rawPlaceholdersToResolvedValues(shortcut.password, variables),
                    authToken = Variables.rawPlaceholdersToResolvedValues(shortcut.authToken, variables),
                    body = Variables.rawPlaceholdersToResolvedValues(shortcut.bodyContent, variables),
                    proxyHost = shortcut.proxyHost
                        ?.let {
                            Variables.rawPlaceholdersToResolvedValues(it, variables)
                        }
                        ?.trim(),
                )
            }
            .flatMap { requestData ->
                makeRequest(context, shortcut, variableManager, requestData, responseFileStorage, fileUploadManager, cookieJar)
                    .onErrorResumeNext { error ->
                        if (error is UnknownHostException && ServiceDiscoveryHelper.isDiscoverable(requestData.uri)) {
                            ServiceDiscoveryHelper.discoverService(context, requestData.uri.host!!)
                                .map { newHost ->
                                    requestData.copy(
                                        url = requestData.uri
                                            .buildUpon()
                                            .encodedAuthority("${newHost.address}:${newHost.port}")
                                            .build()
                                            .toString()
                                    )
                                }
                                .onErrorResumeNext { discoveryError ->
                                    if (discoveryError is ServiceDiscoveryHelper.ServiceLookupTimeoutException) {
                                        Single.just(requestData)
                                    } else {
                                        Single.error(error)
                                    }
                                }
                                .flatMap { newRequestData ->
                                    makeRequest(context, shortcut, variableManager, newRequestData, responseFileStorage, fileUploadManager, cookieJar)
                                }
                        } else {
                            Single.error(error)
                        }
                    }
            }

    private fun makeRequest(
        context: Context,
        shortcut: ShortcutModel,
        variableManager: VariableManager,
        requestData: RequestData,
        responseFileStorage: ResponseFileStorage,
        fileUploadManager: FileUploadManager? = null,
        cookieJar: CookieJar? = null,
    ): Single<ShortcutResponse> =
        Single
            .create<ShortcutResponse> { emitter ->
                val variables = variableManager.getVariableValuesByIds()
                val useDigestAuth = shortcut.authenticationType == ShortcutAuthenticationType.DIGEST
                val client = HttpClients.getClient(
                    context = context,
                    clientCertParams = shortcut.clientCertParams,
                    acceptAllCertificates = shortcut.acceptAllCertificates,
                    username = requestData.username.takeIf { useDigestAuth },
                    password = requestData.password.takeIf { useDigestAuth },
                    followRedirects = shortcut.followRedirects,
                    timeout = shortcut.timeout.toLong(),
                    proxyHost = requestData.proxyHost,
                    proxyPort = shortcut.proxyPort,
                    cookieJar = cookieJar,
                )

                val request = RequestBuilder(shortcut.method, requestData.url)
                    .header(HttpHeaders.CONNECTION, "close")
                    .userAgent(UserAgentUtil.userAgent)
                    .runIf(shortcut.usesCustomBody()) {
                        contentType(determineContentType(shortcut))
                            .body(requestData.body)
                    }
                    .runIf(shortcut.usesFileBody()) {
                        val file = fileUploadManager?.getFile(0)
                        runIfNotNull(file) {
                            contentType(determineContentType(shortcut) ?: it.mimeType)
                                .body(contentResolver.openInputStream(it.data)!!, length = it.fileSize)
                        }
                    }
                    .runIf(shortcut.usesRequestParameters()) {
                        contentType(determineContentType(shortcut))
                            .run {
                                attachParameters(this, shortcut, variables, fileUploadManager)
                            }
                    }
                    .runFor(shortcut.headers) { header ->
                        header(
                            Variables.rawPlaceholdersToResolvedValues(header.key, variables),
                            Variables.rawPlaceholdersToResolvedValues(header.value, variables)
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

                client
                    .newCall(request)
                    .execute()
                    .use { okHttpResponse ->
                        val contentFile = if (shortcut.usesResponseBody) {
                            responseFileStorage.store(okHttpResponse)
                        } else {
                            null
                        }
                        val shortcutResponse = prepareResponse(requestData.url, okHttpResponse, contentFile)
                        if (okHttpResponse.code in 200..399) {
                            emitter.onSuccess(shortcutResponse)
                        } else {
                            emitter.onError(ErrorResponse(shortcutResponse))
                        }
                    }
            }
            .subscribeOn(Schedulers.io())

    private fun attachParameters(
        requestBuilder: RequestBuilder,
        shortcut: ShortcutModel,
        variables: Map<VariableKey, String>,
        fileUploadManager: FileUploadManager?,
    ): RequestBuilder {
        var fileIndex = -1
        return requestBuilder.runFor(shortcut.parameters) { parameter ->
            val parameterName = Variables.rawPlaceholdersToResolvedValues(parameter.key, variables)
            when {
                parameter.isFilesParameter -> {
                    runIfNotNull(fileUploadManager) {
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
                parameter.isFileParameter -> {
                    runIfNotNull(fileUploadManager) {
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
                else -> {
                    parameter(
                        name = parameterName,
                        value = Variables.rawPlaceholdersToResolvedValues(parameter.value, variables),
                    )
                }
            }
        }
    }

    companion object {

        private fun prepareResponse(url: String, response: Response, contentFile: Uri?) =
            ShortcutResponse(
                url = url,
                headers = HttpHeaders.parse(response.headers),
                statusCode = response.code,
                contentFile = contentFile,
                timing = response.receivedResponseAtMillis - response.sentRequestAtMillis,
            )

        private fun determineContentType(shortcut: ShortcutModel): String? =
            when (shortcut.bodyType) {
                RequestBodyType.FORM_DATA -> FORM_MULTIPART_CONTENT_TYPE
                RequestBodyType.X_WWW_FORM_URLENCODE -> FORM_URLENCODE_CONTENT_TYPE
                else -> shortcut.contentType.takeUnlessEmpty()
            }
    }
}
