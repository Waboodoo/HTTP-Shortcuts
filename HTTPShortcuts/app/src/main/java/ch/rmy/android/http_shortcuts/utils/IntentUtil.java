package ch.rmy.android.http_shortcuts.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ch.rmy.android.http_shortcuts.activities.ExecuteActivity;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class IntentUtil {

    private static final String ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String ACTION_UNINSTALL_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    private static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    public static Intent createIntent(Context context, long shortcutId) {
        return createIntent(context, shortcutId, null);
    }

    public static Intent createIntent(Context context, long shortcutId, HashMap<String, String> variableValues) {
        Intent intent = new Intent(context, ExecuteActivity.class);
        intent.setAction(ExecuteActivity.ACTION_EXECUTE_SHORTCUT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (variableValues != null) {
            intent.putExtra(ExecuteActivity.EXTRA_VARIABLE_VALUES, variableValues);
        }

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

    public static Intent getShortcutPlacementIntent(Context context, Shortcut shortcut, boolean install) {
        Intent shortcutIntent = IntentUtil.createIntent(context, shortcut.getId());
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.getName());
        addIntent.putExtra(EXTRA_SHORTCUT_DUPLICATE, true);
        if (shortcut.getIconName() != null) {
            Uri iconUri = shortcut.getIconURI(context);
            Bitmap icon;
            try {
                icon = MediaStore.Images.Media.getBitmap(context.getContentResolver(), iconUri);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
            } catch (Exception e) {
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), ShortcutUIUtils.DEFAULT_ICON));
            }
        } else {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context.getApplicationContext(), ShortcutUIUtils.DEFAULT_ICON));
        }

        addIntent.setAction(install ? ACTION_INSTALL_SHORTCUT : ACTION_UNINSTALL_SHORTCUT);

        return addIntent;
    }

}
