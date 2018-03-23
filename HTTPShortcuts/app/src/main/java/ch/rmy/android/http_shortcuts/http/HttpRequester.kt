package ch.rmy.android.http_shortcuts.http

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import org.jdeferred.Promise

object HttpRequester {

    fun executeShortcut(context: Context, detachedShortcut: Shortcut, variables: ResolvedVariables): Promise<ShortcutResponse, VolleyError, Unit> {

        val url = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.url, variables)
        val username = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.username, variables)
        val password = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.password, variables)
        val body = Variables.rawPlaceholdersToResolvedValues(detachedShortcut.bodyContent, variables)
        val acceptAllCertificates = detachedShortcut.acceptAllCertificates

        val request = ShortcutRequest.Builder(detachedShortcut.method, url)
                .body(body)
                .timeout(detachedShortcut.timeout)
                .mapIf(detachedShortcut.usesBasicAuthentication()) {
                    it.basicAuth(username, password)
                }
                .mapFor(detachedShortcut.parameters) { builder, parameter ->
                    builder.parameter(
                            Variables.rawPlaceholdersToResolvedValues(parameter.key, variables),
                            Variables.rawPlaceholdersToResolvedValues(parameter.value, variables)
                    )
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

    private fun getQueue(context: Context, acceptAllCertificates: Boolean, username: String? = null, password: String? = null): RequestQueue {
        val client = HttpClients.getClient(acceptAllCertificates, username, password)
        return Volley.newRequestQueue(context, OkHttpStack(client))
    }

}
