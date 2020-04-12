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

    fun executeShortcut(shortcut: Shortcut, variableManager: VariableManager): Single<ShortcutResponse> =
        Single
            .create<ShortcutResponse> { emitter ->
                val variables = variableManager.getVariableValuesByIds()

                val url = Variables.rawPlaceholdersToResolvedValues(shortcut.url, variables)
                val username = Variables.rawPlaceholdersToResolvedValues(shortcut.username, variables)
                val password = Variables.rawPlaceholdersToResolvedValues(shortcut.password, variables)
                val authToken = Variables.rawPlaceholdersToResolvedValues(shortcut.authToken, variables)
                val body = Variables.rawPlaceholdersToResolvedValues(shortcut.bodyContent, variables)
                val acceptAllCertificates = shortcut.acceptAllCertificates
                val followRedirects = shortcut.followRedirects

                if (!Validation.isValidUrl(Uri.parse(url))) {
                    emitter.onError(InvalidUrlException())
                    return@create
                }

                val client = HttpClients.getClient(
                    acceptAllCertificates,
                    username.takeIf { shortcut.usesDigestAuthentication() },
                    password.takeIf { shortcut.usesDigestAuthentication() },
                    followRedirects,
                    shortcut.timeout.toLong()
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
                        it.mapFor(shortcut.parameters) { builder, parameter ->
                            builder.parameter(
                                Variables.rawPlaceholdersToResolvedValues(parameter.key, variables),
                                Variables.rawPlaceholdersToResolvedValues(parameter.value, variables)
                            )
                        }
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
                try {
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
                } catch (e: Exception) {
                    emitter.onError(e)
                }
            }
            .subscribeOn(Schedulers.io())

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
