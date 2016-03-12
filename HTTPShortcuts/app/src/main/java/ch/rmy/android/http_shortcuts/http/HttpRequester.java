package ch.rmy.android.http_shortcuts.http;

import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;

import java.util.List;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.shortcuts.Header;
import ch.rmy.android.http_shortcuts.shortcuts.PostParameter;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;
import ch.rmy.android.http_shortcuts.shortcuts.ShortcutStorage;

public class HttpRequester {

    private static final int TOAST_MAX_LENGTH = 400;

    public static void executeShortcut(final Context context, final Shortcut shortcut, final ShortcutStorage shortcutStorage) {
        if (shortcut != null) {

            if (isNetworkConnected(context) || shortcut.getRetryPolicy() == Shortcut.RETRY_POLICY_NONE) {
                final List<PostParameter> parameters;
                if (shortcut.getMethod().equals(Shortcut.METHOD_GET)) {
                    parameters = null;
                } else {
                    parameters = shortcutStorage.getPostParametersByID(shortcut.getID());
                }

                final List<Header> headers = shortcutStorage.getHeadersByID(shortcut.getID());

                HttpRequester.executeShortcut(context, shortcut, parameters, headers);
            } else {
                if (shortcut.getFeedback() != Shortcut.FEEDBACK_NONE) {
                    Toast.makeText(context, String.format(context.getText(R.string.execution_delayed).toString(), shortcut.getName()), Toast.LENGTH_LONG).show();
                }
                shortcutStorage.markShortcutAsPending(shortcut);
            }
        } else {
            Toast.makeText(context, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
        }
    }

    public static void executeShortcut(final Context context, final Shortcut shortcut, final List<PostParameter> parameters, final List<Header> headers) {
        String url = shortcut.getProtocol() + "://" + shortcut.getURL();
        int method = getMethod(shortcut);

        OkHttpClient client = HttpClients.getDefaultOkHttpClient();
        RequestQueue queue = Volley.newRequestQueue(context, new OkHttpStack(client));

        AuthRequest stringRequest = new AuthRequest(method, url, shortcut.getUsername(), shortcut.getPassword(), shortcut.getBodyContent(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                switch (shortcut.getFeedback()) {
                    case Shortcut.FEEDBACK_SIMPLE:
                        Toast.makeText(context, String.format(context.getText(R.string.executed).toString(), shortcut.getName()), Toast.LENGTH_SHORT).show();
                        break;
                    case Shortcut.FEEDBACK_FULL_RESPONSE:
                        String message = response;
                        if (message.length() > TOAST_MAX_LENGTH) {
                            message = message.substring(0, TOAST_MAX_LENGTH) + "…";
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (shortcut.getFeedback() == Shortcut.FEEDBACK_NONE) {
                    return;
                }

                String message;
                if (error.networkResponse != null) {
                    message = String.format(context.getText(R.string.error_http).toString(), shortcut.getName(), error.networkResponse.statusCode);
                } else {
                    if (error.getCause() != null && error.getCause().getMessage() != null) {
                        message = String.format(context.getText(R.string.error_other).toString(), shortcut.getName(), error.getCause().getMessage());
                    } else if (error.getMessage() != null) {
                        message = String.format(context.getText(R.string.error_other).toString(), shortcut.getName(), error.getMessage());
                    } else {
                        message = String.format(context.getText(R.string.error_other).toString(), shortcut.getName(), error.getClass().getSimpleName());
                    }
                    error.printStackTrace();
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }

        });

        if (parameters != null) {
            for (PostParameter parameter : parameters) {
                stringRequest.addParameter(parameter.getKey(), parameter.getValue());
            }
        }

        for (Header header : headers) {
            stringRequest.addHeader(header.getKey(), header.getValue());
        }

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(shortcut.getTimeout(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

    private static int getMethod(Shortcut shortcut) {
        String m = shortcut.getMethod();
        int method = Request.Method.GET;
        if (m.equals(Shortcut.METHOD_POST)) {
            method = Request.Method.POST;
        } else if (m.equals(Shortcut.METHOD_PUT)) {
            method = Request.Method.PUT;
        } else if (m.equals(Shortcut.METHOD_DELETE)) {
            method = Request.Method.DELETE;
        } else if (m.equals(Shortcut.METHOD_PATCH)) {
            method = Request.Method.PATCH;
        }
        return method;
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
