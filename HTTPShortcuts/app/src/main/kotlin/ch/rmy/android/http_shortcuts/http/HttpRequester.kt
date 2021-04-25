package ch.rmy.android.http_shortcuts.http

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_MULTIPART_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.http.RequestUtil.FORM_URLENCODE_CONTENT_TYPE
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.CookieJar
import okhttp3.Response

class HttpRequester(private val contentResolver: ContentResolver) {

    fun executeShortcut(
        context: Context,
        shortcut: Shortcut,
        variableManager: VariableManager,
        responseFileStorage: ResponseFileStorage,
        fileUploadManager: FileUploadManager? = null,
        cookieJar: CookieJar? = null,
    ): Single<ShortcutResponse> =
        Single
            .create<ShortcutResponse> { emitter ->
                val variables = variableManager.getVariableValuesByIds()

                val url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variables).trim()
                val username = Variables.rawPlaceholdersToResolvedValues(shortcut.username, variables)
                val password = Variables.rawPlaceholdersToResolvedValues(shortcut.password, variables)
                val authToken = Variables.rawPlaceholdersToResolvedValues(shortcut.authToken, variables)
                val body = Variables.rawPlaceholdersToResolvedValues(shortcut.bodyContent, variables)
                val proxyHost = shortcut.proxyHost
                    ?.let {
                        Variables.rawPlaceholdersToResolvedValues(it, variables)
                    }
                    ?.trim()

                if (!Validation.isValidHttpUrl(Uri.parse(url))) {
                    emitter.onError(InvalidUrlException(url))
                    return@create
                }

                val client = HttpClients.getClient(
                    context = context,
                    clientCertParams = shortcut.clientCertParams,
                    acceptAllCertificates = shortcut.acceptAllCertificates,
                    username = username.takeIf { shortcut.usesDigestAuthentication() },
                    password = password.takeIf { shortcut.usesDigestAuthentication() },
                    followRedirects = shortcut.followRedirects,
                    timeout = shortcut.timeout.toLong(),
                    proxyHost = proxyHost,
                    proxyPort = shortcut.proxyPort,
                    cookieJar = cookieJar,
                )

                val request = RequestBuilder(shortcut.method, url)
                    .header(HttpHeaders.CONNECTION, "close")
                    .userAgent(UserAgentUtil.userAgent)
                    .mapIf(shortcut.usesCustomBody()) {
                        contentType(determineContentType(shortcut))
                            .body(body)
                    }
                    .mapIf(shortcut.usesFileBody()) {
                        val file = fileUploadManager?.getFile(0)
                        mapIf(file != null) {
                            contentType(determineContentType(shortcut) ?: file!!.mimeType)
                                .body(contentResolver.openInputStream(file!!.data)!!, length = file.fileSize)
                        }
                    }
                    .mapIf(shortcut.usesRequestParameters()) {
                        contentType(determineContentType(shortcut))
                            .run {
                                attachParameters(this, shortcut, variables, fileUploadManager)
                            }
                    }
                    .mapFor(shortcut.headers) { header ->
                        header(
                            Variables.rawPlaceholdersToResolvedValues(header.key, variables),
                            Variables.rawPlaceholdersToResolvedValues(header.value, variables)
                        )
                    }
                    .mapIf(shortcut.usesBasicAuthentication()) {
                        basicAuth(username, password)
                    }
                    .mapIf(shortcut.usesBearerAuthentication()) {
                        bearerAuth(authToken)
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
                        val shortcutResponse = prepareResponse(url, okHttpResponse, contentFile)
                        if (okHttpResponse.isSuccessful) {
                            emitter.onSuccess(shortcutResponse)
                        } else {
                            emitter.onError(ErrorResponse(shortcutResponse))
                        }
                    }
            }
            .subscribeOn(Schedulers.io())

    private fun attachParameters(requestBuilder: RequestBuilder, shortcut: Shortcut, variables: Map<String, String>, fileUploadManager: FileUploadManager?): RequestBuilder {
        var fileIndex = -1
        return requestBuilder.mapFor(shortcut.parameters) { parameter ->
            val parameterName = Variables.rawPlaceholdersToResolvedValues(parameter.key, variables)
            when {
                parameter.isFilesParameter -> {
                    mapIf(fileUploadManager != null) {
                        fileIndex++
                        val files = fileUploadManager!!.getFiles(fileIndex)
                        mapFor(files) { file ->
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
                    mapIf(fileUploadManager != null) {
                        fileIndex++
                        val file = fileUploadManager!!.getFile(fileIndex)
                        mapIf(file != null) {
                            fileParameter(
                                name = parameterName,
                                fileName = parameter.fileName.ifEmpty { file!!.fileName },
                                type = file!!.mimeType,
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
                headers = HttpHeaders.parse(response.headers()),
                statusCode = response.code(),
                contentFile = contentFile,
                timing = response.receivedResponseAtMillis() - response.sentRequestAtMillis(),
            )

        private fun determineContentType(shortcut: Shortcut): String? =
            when (shortcut.requestBodyType) {
                Shortcut.REQUEST_BODY_TYPE_FORM_DATA -> FORM_MULTIPART_CONTENT_TYPE
                Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> FORM_URLENCODE_CONTENT_TYPE
                else -> shortcut.contentType.takeUnlessEmpty()
            }

    }

}
