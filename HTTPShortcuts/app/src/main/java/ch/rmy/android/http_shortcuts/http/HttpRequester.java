package ch.rmy.android.http_shortcuts.http;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class HttpRequester {

    public static Promise<String, VolleyError, Void> executeShortcut(final Context context, final Shortcut detachedShortcut) {
        String url = detachedShortcut.getUrl();
        int method = getMethod(detachedShortcut);

        OkHttpClient client = HttpClients.getDefaultOkHttpClient();
        RequestQueue queue = Volley.newRequestQueue(context, new OkHttpStack(client));

        final Deferred<String, VolleyError, Void> deferred = new DeferredObject<>();

        Promise<String, VolleyError, Void> promise = deferred.promise();

        AuthRequest stringRequest = new AuthRequest(method, url, detachedShortcut.getUsername(), detachedShortcut.getPassword(), detachedShortcut.getBodyContent(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                deferred.resolve(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                deferred.reject(error);
            }
        });

        for (Parameter parameter : detachedShortcut.getParameters()) {
            stringRequest.addParameter(parameter.getKey(), parameter.getValue());
        }

        for (Header header : detachedShortcut.getHeaders()) {
            stringRequest.addHeader(header.getKey(), header.getValue());
        }

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(detachedShortcut.getTimeout(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);

        return promise;
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
