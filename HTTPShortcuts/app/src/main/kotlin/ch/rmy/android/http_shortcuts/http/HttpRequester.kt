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

    fun executeShortcut(detachedShortcut: Shortcut, variableManager: VariableManager): Single<ShortcutResponse> =
        Single
            .create<ShortcutResponse> { emitter ->
                val variables = variableManager.getVariableValuesByIds()

                val url = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.url, variables)
                val username = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.username, variables)
                val password = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.password, variables)
                val body = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.bodyContent, variables)
                val acceptAllCertificates = detachedShortcut.acceptAllCertificates
                val followRedirects = detachedShortcut.followRedirects

                if (!Validation.isValidUrl(Uri.parse(url))) {
                    emitter.onError(InvalidUrlException())
                    return@create
                }

                val client = HttpClients.getClient(
                    acceptAllCertificates,
                    username.takeIf { detachedShortcut.usesDigestAuthentication() },
                    password.takeIf { detachedShortcut.usesDigestAuthentication() },
                    followRedirects,
                    detachedShortcut.timeout.toLong()
                )

                val request = RequestBuilder(detachedShortcut.method, url)
                    .header(HttpHeaders.CONNECTION, "close")
                    .header(HttpHeaders.USER_AGENT, UserAgentUtil.userAgent)
                    .mapIf(detachedShortcut.usesCustomBody()) {
                        it.contentType(determineContentType(detachedShortcut))
                        it.body(body)
                    }
                    .mapIf(detachedShortcut.usesRequestParameters()) {
                        it.mapFor(detachedShortcut.parameters) { builder, parameter ->
                            builder.parameter(
                                Variables.rawPlaceholdersToResolvedValues(parameter.key, variables),
                                Variables.rawPlaceholdersToResolvedValues(parameter.value, variables)
                            )
                        }
                    }
                    .mapFor(detachedShortcut.headers) { builder, header ->
                        builder.header(
                            Variables.rawPlaceholdersToResolvedValues(header.key, variables),
                            Variables.rawPlaceholdersToResolvedValues(header.value, variables)
                        )
                    }
                    .mapIf(detachedShortcut.usesBasicAuthentication()) {
                        it.basicAuth(username, password)
                    }
                    .build()
                val call = client.newCall(request)

                try {
                    val okHttpResponse = call.execute()
                    val shortcutResponse = prepareResponse(url, okHttpResponse)
                    okHttpResponse.close()

                    if (okHttpResponse.isSuccessful) {
                        emitter.onSuccess(shortcutResponse)
                    } else {
                        emitter.onError(ErrorResponse(shortcutResponse))
                    }
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
            .subscribeOn(Schedulers.io())

    private fun prepareResponse(url: String, response: Response): ShortcutResponse {
        val responseHeaders = response.headers.toMap()
        val responseBody = response.body

        return ShortcutResponse(
            url = url,
            headers = responseHeaders,
            statusCode = response.code,
            contentLength = responseBody?.contentLength()?.toInt(),
            content = responseBody?.byteStream()
        )
    }

    private fun determineContentType(shortcut: Shortcut): String? = when {
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA -> "multipart/form-data; boundary=${RequestBuilder.FORM_MULTIPART_BOUNDARY}"
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> "application/x-www-form-urlencoded; charset=UTF-8"
        shortcut.contentType.isNotEmpty() -> shortcut.contentType
        else -> null
    }

}
