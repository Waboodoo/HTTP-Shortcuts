package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.reactivex.Single

object HttpRequester {

    fun executeShortcut(context: Context, detachedShortcut: Shortcut, variableManager: VariableManager): Single<ShortcutResponse> {
        return Single.create { emitter ->
            val variables = variableManager.getVariableValuesByIds()

            val url = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.url, variables)
            val username = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.username, variables)
            val password = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.password, variables)
            val body = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.bodyContent, variables)
            val acceptAllCertificates = detachedShortcut.acceptAllCertificates

            if (!Validation.isValidUrl(Uri.parse(url))) {
                emitter.onError(Exception(context.getString(R.string.error_invalid_url)))
                return@create
            }

            val request = ShortcutRequest.Builder(detachedShortcut.method, url, emitter)
                .mapIf(detachedShortcut.usesCustomBody()) {
                    it.body(body)
                }
                .contentType(determineContentType(detachedShortcut))
                .timeout(detachedShortcut.timeout)
                .mapIf(detachedShortcut.usesBasicAuthentication()) {
                    it.basicAuth(username, password)
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
                .build()

            getQueue(
                context,
                acceptAllCertificates,
                username.takeIf { detachedShortcut.usesDigestAuthentication() },
                password.takeIf { detachedShortcut.usesDigestAuthentication() },
                detachedShortcut.followRedirects
            )
                .add(request)

        }
    }

    private fun determineContentType(shortcut: Shortcut): String = when {
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA -> "multipart/form-data; boundary=${ShortcutRequest.FORM_MULTIPART_BOUNDARY}"
        shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> "application/x-www-form-urlencoded; charset=UTF-8"
        shortcut.contentType.isNotEmpty() -> shortcut.contentType
        else -> Shortcut.DEFAULT_CONTENT_TYPE
    }

    private fun getQueue(context: Context, acceptAllCertificates: Boolean, username: String? = null, password: String? = null, followRedirects: Boolean = false): RequestQueue {
        val client = HttpClients.getClient(acceptAllCertificates, username, password, followRedirects)
        return Volley.newRequestQueue(context, OkHttpStack(client))
    }

}
