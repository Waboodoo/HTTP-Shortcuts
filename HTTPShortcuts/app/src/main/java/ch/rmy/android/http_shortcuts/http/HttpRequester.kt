package ch.rmy.android.http_shortcuts.http

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import org.jdeferred2.Promise

object HttpRequester {

    fun executeShortcut(context: Context, detachedShortcut: Shortcut, variables: Map<String, String>): Promise<ShortcutResponse, VolleyError, Unit> {
        val url = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.url, variables)
        val username = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.username, variables)
        val password = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.password, variables)
        val body = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.bodyContent, variables)
        val acceptAllCertificates = detachedShortcut.acceptAllCertificates

        val request = ShortcutRequest.Builder(detachedShortcut.method, url)
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
                if (detachedShortcut.usesDigestAuthentication()) username else null,
                if (detachedShortcut.usesDigestAuthentication()) password else null
        )
                .add(request)

        return request.promise
    }

    private fun determineContentType(shortcut: Shortcut): String {
        return when {
            shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA -> "multipart/form-data; boundary=${ShortcutRequest.FORM_MULTIPART_BOUNDARY}"
            shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> "application/x-www-form-urlencoded; charset=UTF-8"
            shortcut.contentType.isNotEmpty() -> shortcut.contentType
            else -> Shortcut.DEFAULT_CONTENT_TYPE
        }
    }

    private fun getQueue(context: Context, acceptAllCertificates: Boolean, username: String? = null, password: String? = null): RequestQueue {
        val client = HttpClients.getClient(acceptAllCertificates, username, password)
        return Volley.newRequestQueue(context, OkHttpStack(client))
    }

}
