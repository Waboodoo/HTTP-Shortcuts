package ch.rmy.android.http_shortcuts.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.rmy.android.http_shortcuts.ExecuteActivity;

public class IntentUtil {

    public static Intent createIntent(Context context, long shortcutId) {
        Intent intent = new Intent(context, ExecuteActivity.class);
        intent.setAction(ExecuteActivity.ACTION_EXECUTE_SHORTCUT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        Uri uri = ContentUris.withAppendedId(Uri.fromParts("content", context.getPackageName(), null), shortcutId);
        intent.setData(uri);
        return intent;
    }

    public static long getShortcutId(Intent intent) {
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
            return intent.getLongExtra(ExecuteActivity.EXTRA_SHORTCUT_ID, -1); // for backwards compatibility
        }
        return shortcutId;
    }

    public static Map<String, String> getVariableValues(Intent intent) {
        Serializable serializable = intent.getSerializableExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES);
        if (serializable instanceof Map) {
            return (Map<String, String>) serializable;
        }
        return new HashMap<>();
    }

}
