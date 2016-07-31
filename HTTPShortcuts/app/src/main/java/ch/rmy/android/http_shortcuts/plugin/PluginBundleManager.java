package ch.rmy.android.http_shortcuts.plugin;

import android.os.Bundle;

public class PluginBundleManager {

    private static final String SHORTCUT_ID = "ch.rmy.android.http_shortcuts.shortcut_id";

    public static Bundle generateBundle(long shortcutId) {
        Bundle bundle = new Bundle();
        bundle.putLong(SHORTCUT_ID, shortcutId);
        return bundle;
    }

    public static long getShortcutId(Bundle bundle) {
        return bundle.getLong(SHORTCUT_ID);
    }

    public static boolean isBundleValid(Bundle bundle) {
        return bundle != null && bundle.containsKey(SHORTCUT_ID);
    }

}
