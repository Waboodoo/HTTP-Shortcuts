package ch.rmy.android.http_shortcuts.http;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class AuthRequest extends StringRequest {

    private final String bodyContent;
    private final Map<String, String> parameters;
    private final Map<String, String> headers;
    private String contentType;

    public AuthRequest(int method, String url, String username, String password, String bodyContent, Listener<String> listener, ErrorListener errorListener) {
        super(method, url, listener, errorListener);

        this.bodyContent = bodyContent;

        parameters = new HashMap<>();
        headers = new HashMap<>();

        headers.put("Connection", "close");
        if (!username.isEmpty() || !password.isEmpty()) {
            String authorization = String.format("%s:%s", username, password);
            String encodedAuthorization = "Basic " + Base64.encodeToString(authorization.getBytes(), Base64.NO_WRAP);
            headers.put("Authorization", encodedAuthorization);
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (bodyContent.isEmpty()) {
            return super.getBody();
        } else {
            byte[] regularBody = super.getBody();
            byte[] customBody = bodyContent.getBytes();
            if (regularBody == null) {
                return customBody;
            }
            byte[] mergedBody = new byte[regularBody.length + customBody.length];

            System.arraycopy(regularBody, 0, mergedBody, 0, regularBody.length);
            System.arraycopy(customBody, 0, mergedBody, regularBody.length, customBody.length);

            return mergedBody;
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers;
    }

    @Override
    public String getBodyContentType() {
        if (contentType != null) {
            return contentType;
        }
        return super.getBodyContentType();
    }

    @Override
    public Map<String, String> getParams() {
        return parameters;
    }

    public void addParameter(String key, String value) {
        parameters.put(key, value);
    }

    public void addHeader(String key, String value) {
        if (key.equals("Content-Type")) {
            contentType = value;
        } else {
            headers.put(key, value);
        }
    }

}
