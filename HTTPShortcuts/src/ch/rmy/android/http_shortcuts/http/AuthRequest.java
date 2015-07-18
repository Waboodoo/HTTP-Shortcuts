package ch.rmy.android.http_shortcuts.http;

import java.util.HashMap;
import java.util.Map;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class AuthRequest extends StringRequest {

	private final String username;
	private final String password;
	private Map<String, String> parameters;

	public AuthRequest(int method, String url, String username, String password, Listener<String> listener, ErrorListener errorListener) {
		super(method, url, listener, errorListener);

		this.username = username;
		this.password = password;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>(super.getHeaders());

		params.put("Connection", "close");

		if (!username.isEmpty() || !password.isEmpty()) {
			String creds = String.format("%s:%s", username, password);
			String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
			params.put("Authorization", auth);
		}
		return params;
	}

	@Override
	public Map<String, String> getParams() {
		return parameters;
	}

	public void setParams(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
