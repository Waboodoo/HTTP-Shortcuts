package ch.rmy.android.http_shortcuts.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.jdeferred.Promise;

import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.Variables;
import okhttp3.OkHttpClient;

public class HttpRequester {

    public static Promise<ShortcutResponse, VolleyError, Void> executeShortcut(final Context context, final Shortcut detachedShortcut, ResolvedVariables variables) {
        final String url = Variables.INSTANCE.insert(detachedShortcut.getUrl(), variables);
        final String username = Variables.INSTANCE.insert(detachedShortcut.getUsername(), variables);
        final String password = Variables.INSTANCE.insert(detachedShortcut.getPassword(), variables);
        final String body = Variables.INSTANCE.insert(detachedShortcut.getBodyContent(), variables);
        final boolean acceptAllCertificates = detachedShortcut.getAcceptAllCertificates();

        ShortcutRequest.Builder builder = new ShortcutRequest.Builder(detachedShortcut.getMethod(), url)
                .body(body)
                .timeout(detachedShortcut.getTimeout());

        if (detachedShortcut.usesBasicAuthentication()) {
            builder = builder.basicAuth(username, password);
        }

        for (Parameter parameter : detachedShortcut.getParameters()) {
            builder = builder.parameter(
                    Variables.INSTANCE.insert(parameter.getKey(), variables),
                    Variables.INSTANCE.insert(parameter.getValue(), variables)
            );
        }

        for (Header header : detachedShortcut.getHeaders()) {
            builder = builder.header(
                    Variables.INSTANCE.insert(header.getKey(), variables),
                    Variables.INSTANCE.insert(header.getValue(), variables)
            );
        }

        ShortcutRequest request = builder.build();
        getQueue(
                context,
                acceptAllCertificates,
                detachedShortcut.usesDigestAuthentication() ? username : null,
                detachedShortcut.usesDigestAuthentication() ? password : null
        ).add(request);

        return request.getPromise();
    }

    private static RequestQueue getQueue(Context context, boolean acceptAllCertificates, String username, String password) {
        OkHttpClient client = HttpClients.getClient(acceptAllCertificates, username, password);
        return Volley.newRequestQueue(context, new OkHttpStack(client));
    }

}
