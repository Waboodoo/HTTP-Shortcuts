package ch.rmy.android.http_shortcuts.http

import android.net.Uri
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Response

object HttpRequester {

    fun executeShortcut(shortcut: Shortcut, variableManager: VariableManager, fileUploadManager: FileUploadManager? = null): Single<ShortcutResponse> =
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

                if (!Validation.isValidUrl(Uri.parse(url))) {
                    emitter.onError(InvalidUrlException(url))
                    return@create
                }

                val client = HttpClients.getClient(
                    acceptAllCertificates = shortcut.acceptAllCertificates,
                    username = username.takeIf { shortcut.usesDigestAuthentication() },
                    password = password.takeIf { shortcut.usesDigestAuthentication() },
                    followRedirects = shortcut.followRedirects,
                    timeout = shortcut.timeout.toLong(),
                    proxyHost = proxyHost,
                    proxyPort = shortcut.proxyPort
                )

                val request = RequestBuilder(shortcut.method, url)
                    .header(HttpHeaders.CONNECTION, "close")
                    .header(HttpHeaders.USER_AGENT, UserAgentUtil.userAgent)
                    .mapIf(shortcut.usesCustomBody()) {
                        it.contentType(determineContentType(shortcut))
                        it.body(body)
                    }
                    .mapIf(shortcut.usesRequestParameters()) {
                        it.contentType(determineContentType(shortcut))
                        attachParameters(it, shortcut, variables, fileUploadManager)
                    }
                    .mapFor(shortcut.headers) { builder, header ->
                        builder.header(
                            Variables.rawPlaceholdersToResolvedValues(header.key, variables),
                            Variables.rawPlaceholdersToResolvedValues(header.value, variables)
                        )
                    }
                    .mapIf(shortcut.usesBasicAuthentication()) {
                        it.basicAuth(username, password)
                    }
                    .mapIf(shortcut.usesBearerAuthentication()) {
                        it.bearerAuth(authToken)
                    }
                    .build()

                client
                    .newCall(request)
                    .execute()
                    .use { okHttpResponse ->
                        val shortcutResponse = prepareResponse(url, okHttpResponse, ignoreBody = !shortcut.usesResponseBody)
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
        return requestBuilder.mapFor(shortcut.parameters) { builder, parameter ->
            val parameterName = Variables.rawPlaceholdersToResolvedValues(parameter.key, variables)
            when {
                parameter.isFilesParameter -> {
                    builder.mapIf(fileUploadManager != null) { builder2 ->
                        fileIndex++
                        val files = fileUploadManager!!.getFiles(fileIndex)
                        builder2.mapFor(files) { builder3, file ->
                            builder3.fileParameter(
                                name = parameterName + "[]",
                                fileName = parameter.fileName.ifEmpty { file.fileName },
                                type = file.mimeType,
                                data = file.data
                            )
                        }
                    }
                }
                parameter.isFileParameter -> {
                    builder.mapIf(fileUploadManager != null) { builder2 ->
                        fileIndex++
                        val file = fileUploadManager!!.getFile(fileIndex)
                        builder2.mapIf(file != null) { builder3 ->
                            builder3.fileParameter(
                                name = parameterName,
                                fileName = parameter.fileName.ifEmpty { file!!.fileName },
                                type = file!!.mimeType,
                                data = file.data
                            )
                        }
                    }
                }
                else -> {
                    builder.parameter(
                        name = parameterName,
                        value = Variables.rawPlaceholdersToResolvedValues(parameter.value, variables)
                    )
                }
            }
        }
    }

    private fun prepareResponse(url: String, response: Response, ignoreBody: Boolean) =
        ShortcutResponse(
            url = url,
            headers = HttpHeaders.parse(response.headers()),
            statusCode = response.code(),
            content = response.takeUnless { ignoreBody }?.body()?.byteStream(),
            timing = response.receivedResponseAtMillis() - response.sentRequestAtMillis()
        )

    private fun determineContentType(shortcut: Shortcut): String? = when {
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA -> "multipart/form-data; boundary=${RequestBuilder.FORM_MULTIPART_BOUNDARY}"
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> "application/x-www-form-urlencoded; charset=UTF-8"
        shortcut.contentType.isNotEmpty() -> shortcut.contentType
        else -> null
    }

}
