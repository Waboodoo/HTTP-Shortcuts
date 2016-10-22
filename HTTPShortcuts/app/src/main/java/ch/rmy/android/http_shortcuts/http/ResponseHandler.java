package ch.rmy.android.http_shortcuts.http;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class ResponseHandler {

    private static final int TOAST_MAX_LENGTH = 400;

    private final Context context;

    public ResponseHandler(Context context) {
        this.context = context;
    }

    public void handleSuccess(Shortcut shortcut, String response) {
        switch (shortcut.getFeedback()) {
            case Shortcut.FEEDBACK_TOAST_SIMPLE:
                Toast.makeText(context, String.format(context.getText(R.string.executed).toString(), shortcut.getSafeName(context)), Toast.LENGTH_SHORT).show();
                break;
            case Shortcut.FEEDBACK_TOAST:
                String message = response;
                if (message.length() > TOAST_MAX_LENGTH) {
                    message = message.substring(0, TOAST_MAX_LENGTH) + "…";
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                break;
            case Shortcut.FEEDBACK_DIALOG:
                Toast.makeText(context, "Not yet implemented", Toast.LENGTH_LONG).show(); // TODO
        }
    }

    public void handleFailure(Shortcut shortcut, VolleyError error) {
        if (Shortcut.FEEDBACK_NONE.equals(shortcut.getFeedback())) {
            return;
        }

        String name = shortcut.getSafeName(context);

        String message;
        if (error.networkResponse != null) {
            message = String.format(context.getText(R.string.error_http).toString(), name, error.networkResponse.statusCode);
        } else {
            if (error.getCause() != null && error.getCause().getMessage() != null) {
                message = String.format(context.getText(R.string.error_other).toString(), name, error.getCause().getMessage());
            } else if (error.getMessage() != null) {
                message = String.format(context.getText(R.string.error_other).toString(), name, error.getMessage());
            } else {
                message = String.format(context.getText(R.string.error_other).toString(), name, error.getClass().getSimpleName());
            }
            error.printStackTrace();
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
