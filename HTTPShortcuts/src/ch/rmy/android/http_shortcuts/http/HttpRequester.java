package ch.rmy.android.http_shortcuts.http;

import android.content.Context;
import android.widget.Toast;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.shortcuts.Shortcut;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

public class HttpRequester {

	public static void executeShortcut(final Context context, final Shortcut shortcut) {
		RequestQueue queue = Volley.newRequestQueue(context);

		String url = shortcut.getProtocol() + "://" + shortcut.getURL();
		int method = shortcut.getMethod().equals(Shortcut.METHOD_GET) ? Request.Method.GET : Request.Method.POST;

		AuthRequest stringRequest = new AuthRequest(method, url, shortcut.getUsername(), shortcut.getPassword(), new Response.Listener<String>() {

			@Override
			public void onResponse(String response) {
				switch (shortcut.getFeedback()) {
				case Shortcut.FEEDBACK_SIMPLE:
					Toast.makeText(context, String.format(context.getText(R.string.executed).toString(), shortcut.getName()), Toast.LENGTH_SHORT).show();
					break;
				case Shortcut.FEEDBACK_FULL_RESPONSE:
					String message = response;
					if (message.length() > 200) {
						message = message.substring(0, 200) + "...";
					}
					Toast.makeText(context, message, Toast.LENGTH_LONG).show();
					break;

				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				String message;
				if (error.networkResponse != null) {
					message = String.format(context.getText(R.string.error_http).toString(), shortcut.getName(), error.networkResponse.statusCode);
				} else {
					if (error.getCause() != null) {
						message = error.getCause().getMessage();
					} else if (error.getMessage() != null) {
						message = error.getMessage();
					} else {
						message = "Failed.";
					}
				}
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}

		});
		queue.add(stringRequest);
	}

}
