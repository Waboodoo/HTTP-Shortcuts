package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Settings {

    public static final String CLICK_BEHAVIOR_RUN = "run";
    public static final String CLICK_BEHAVIOR_EDIT = "edit";
    public static final String CLICK_BEHAVIOR_MENU = "menu";

    private static final String KEY_CLICK_BEHAVIOR = "click_behavior";
    private static final String KEY_IMPORT_EXPORT_DIR = "import_export_dir";

    private final SharedPreferences preferences;

    public Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getClickBehavior() {
        return preferences.getString(KEY_CLICK_BEHAVIOR, CLICK_BEHAVIOR_RUN);
    }

    public String getImportExportDirectory() {
        return preferences.getString(KEY_IMPORT_EXPORT_DIR, Environment.getExternalStorageDirectory().getPath());
    }

    public void setImportExportDirectory(String path) {
        if (path == null) {
            return;
        }
        preferences.edit().putString(KEY_IMPORT_EXPORT_DIR, path).apply();
    }

}
