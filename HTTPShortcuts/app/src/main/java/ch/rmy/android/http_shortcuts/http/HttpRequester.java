package ch.rmy.android.http_shortcuts.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

import org.jdeferred.Promise;

import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.Variables;

public class HttpRequester {

    public static Promise<ShortcutResponse, VolleyError, Void> executeShortcut(final Context context, final Shortcut detachedShortcut, ResolvedVariables variables) {
        boolean acceptAllCertificates = detachedShortcut.isAcceptAllCertificates();

        OkHttpClient client = acceptAllCertificates ? HttpClients.getUnsafeOkHttpClient() : HttpClients.getDefaultOkHttpClient();
        RequestQueue queue = Volley.newRequestQueue(context, new OkHttpStack(client));

        final String url = Variables.insert(detachedShortcut.getUrl(), variables);
        final String username = Variables.insert(detachedShortcut.getUsername(), variables);
        final String password = Variables.insert(detachedShortcut.getPassword(), variables);
        final String body = Variables.insert(detachedShortcut.getBodyContent(), variables);

        ShortcutRequest.Builder builder = new ShortcutRequest.Builder(detachedShortcut.getMethod(), url)
                .body(body)
                .timeout(detachedShortcut.getTimeout());

        if (!username.isEmpty() || !password.isEmpty()) {
            builder = builder.basicAuth(username, password);
        }

        for (Parameter parameter : detachedShortcut.getParameters()) {
            builder = builder.parameter(
                    Variables.insert(parameter.getKey(), variables),
                    Variables.insert(parameter.getValue(), variables)
            );
        }

        for (Header header : detachedShortcut.getHeaders()) {
            builder = builder.header(
                    Variables.insert(header.getKey(), variables),
                    Variables.insert(header.getValue(), variables)
            );
        }

        ShortcutRequest request = builder.build();
        queue.add(request);

        return request.getPromise();
    }

}
