package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Promise;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.rmy.android.http_shortcuts.http.Executor;

public class ExecuteActivity extends Activity {

    public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";
    public static final String EXTRA_SHORTCUT_ID = "id";
    public static final String EXTRA_VARIABLE_VALUES = "variable_values";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        long shortcutId = getShortcutId(getIntent());
        Map<String, String> variableValues = getVariableValues(getIntent());
        Executor executor = new Executor(this);
        Promise<Void, Void, Void> promise = executor.execute(shortcutId, variableValues);
        if (promise.isPending()) {
            promise.always(new AlwaysCallback<Void, Void>() {
                @Override
                public void onAlways(Promise.State state, Void resolved, Void rejected) {
                    finishWithoutAnimation();
                }
            });
        } else {
            finishWithoutAnimation();
        }
    }

    private void finishWithoutAnimation() {
        finish();
        overridePendingTransition(0, 0);
    }

    private static long getShortcutId(Intent intent) {
        long shortcutId = -1;
        Uri uri = intent.getData();
        if (uri != null) {
            try {
                String id = uri.getLastPathSegment();
                shortcutId = Long.parseLong(id);
            } catch (NumberFormatException e) {
            }
        }
        if (shortcutId == -1) {
            return intent.getLongExtra(EXTRA_SHORTCUT_ID, -1); // for backwards compatibility
        }
        return shortcutId;
    }

    private Map<String, String> getVariableValues(Intent intent) {
        Serializable serializable = intent.getSerializableExtra(EXTRA_VARIABLE_VALUES);
        if (serializable instanceof Map) {
            return (Map<String, String>) serializable;
        }
        return new HashMap<>();
    }

}
