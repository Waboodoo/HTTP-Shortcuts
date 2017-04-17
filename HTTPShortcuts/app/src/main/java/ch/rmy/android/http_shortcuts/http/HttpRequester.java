package ch.rmy.android.http_shortcuts.http;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.variables.ResolvedVariables;
import ch.rmy.android.http_shortcuts.variables.Variables;

public class HttpRequester {

    public static Promise<ShortcutResponse, VolleyError, Void> executeShortcut(final Context context, final Shortcut detachedShortcut, ResolvedVariables variables) {
        int method = getMethod(detachedShortcut);
        boolean acceptAllCertificates = detachedShortcut.isAcceptAllCertificates();

        OkHttpClient client = acceptAllCertificates ? HttpClients.getUnsafeOkHttpClient() : HttpClients.getDefaultOkHttpClient();
        RequestQueue queue = Volley.newRequestQueue(context, new OkHttpStack(client));

        final Deferred<ShortcutResponse, VolleyError, Void> deferred = new DeferredObject<>();

        ShortcutRequest request = new ShortcutRequest(
                method,
                Variables.insert(detachedShortcut.getUrl(), variables),
                Variables.insert(detachedShortcut.getUsername(), variables),
                Variables.insert(detachedShortcut.getPassword(), variables),
                Variables.insert(detachedShortcut.getBodyContent(), variables),
                new ResponseListener() {
                    @Override
                    public void onResponse(ShortcutResponse response) {
                        deferred.resolve(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                deferred.reject(error);
            }
        });

        for (Parameter parameter : detachedShortcut.getParameters()) {
            request.addParameter(
                    Variables.insert(parameter.getKey(), variables),
                    Variables.insert(parameter.getValue(), variables)
            );
        }

        for (Header header : detachedShortcut.getHeaders()) {
            request.addHeader(
                    Variables.insert(header.getKey(), variables),
                    Variables.insert(header.getValue(), variables)
            );
        }

        request.setRetryPolicy(new DefaultRetryPolicy(detachedShortcut.getTimeout(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

        return deferred.promise();
    }

    private static int getMethod(Shortcut shortcut) {
        switch (shortcut.getMethod()) {
            case Shortcut.METHOD_POST:
                return Request.Method.POST;
            case Shortcut.METHOD_PUT:
                return Request.Method.PUT;
            case Shortcut.METHOD_DELETE:
                return Request.Method.DELETE;
            case Shortcut.METHOD_PATCH:
                return Request.Method.PATCH;
            default:
                return Request.Method.GET;
        }
    }

}
