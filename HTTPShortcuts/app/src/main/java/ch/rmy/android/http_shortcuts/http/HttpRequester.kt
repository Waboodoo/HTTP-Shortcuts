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

    fun executeShortcut(context: Context, detachedShortcut: Shortcut, variables: ResolvedVariables): Promise<ShortcutResponse, VolleyError, Void> {

        val url = Variables.insert(detachedShortcut.url, variables)
        val username = Variables.insert(detachedShortcut.username, variables)
        val password = Variables.insert(detachedShortcut.password, variables)
        val body = Variables.insert(detachedShortcut.bodyContent, variables)
        val acceptAllCertificates = detachedShortcut.acceptAllCertificates

        val request = ShortcutRequest.Builder(detachedShortcut.method, url)
                .body(body)
                .timeout(detachedShortcut.timeout)
                .mapIf(detachedShortcut.usesBasicAuthentication()) {
                    it.basicAuth(username, password)
                }
                .mapFor(detachedShortcut.parameters) { builder, parameter ->
                    builder.parameter(
                            Variables.insert(parameter.key, variables),
                            Variables.insert(parameter.value, variables)
                    )
                }
                .mapFor(detachedShortcut.headers) { builder, header ->
                    builder.header(
                            Variables.insert(header.key, variables),
                            Variables.insert(header.value, variables)
                    )
                }
                .build()

        getQueue(
                context,
                acceptAllCertificates,
                if (detachedShortcut.usesDigestAuthentication()) username else null,
                if (detachedShortcut.usesDigestAuthentication()) password else null
        ).add(request)

        return request.promise
    }

    private fun getQueue(context: Context, acceptAllCertificates: Boolean, username: String? = null, password: String? = null): RequestQueue {
        val client = HttpClients.getClient(acceptAllCertificates, username, password)
        return Volley.newRequestQueue(context, OkHttpStack(client))
    }

}
