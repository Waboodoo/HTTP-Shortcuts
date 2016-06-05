package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import ch.rmy.android.http_shortcuts.http.Executor;

public class ExecuteActivity extends Activity {

    public static final String ACTION_EXECUTE_SHORTCUT = "ch.rmy.android.http_shortcuts.execute";
    public static final String EXTRA_SHORTCUT_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long shortcutId = getShortcutId(getIntent());
        Executor executor = new Executor(this);
        executor.execute(shortcutId);

        finish();
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

}
