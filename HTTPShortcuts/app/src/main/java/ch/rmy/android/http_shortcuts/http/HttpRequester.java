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

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class HttpRequester {

    private static final int TOAST_MAX_LENGTH = 400;

    public static void executeShortcut(final Context context, final long shortcutId, Controller controller) {
        Shortcut shortcut = controller.getDetachedShortcutById(shortcutId);
        if (shortcut != null) {

            if (isNetworkConnected(context) || Shortcut.RETRY_POLICY_NONE.equals(shortcut.getRetryPolicy())) {
                HttpRequester.executeShortcut(context, shortcut);
            } else {
                if (Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
                    Toast.makeText(context, String.format(context.getText(R.string.execution_delayed).toString(), shortcut.getName()), Toast.LENGTH_LONG).show();
                }
                // TODO
                //shortcutStorage.markShortcutAsPending(shortcut);
            }
        } else {
            Toast.makeText(context, R.string.shortcut_not_found, Toast.LENGTH_LONG).show();
        }
    }

    public static void executeShortcut(final Context context, final Shortcut shortcut) {
        String url = shortcut.getUrl();
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
                if (Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
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

        for (Parameter parameter : shortcut.getParameters()) {
            stringRequest.addParameter(parameter.getKey(), parameter.getValue());
        }

        for (Header header : shortcut.getHeaders()) {
            stringRequest.addHeader(header.getKey(), header.getValue());
        }

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(shortcut.getTimeout(), 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
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

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
