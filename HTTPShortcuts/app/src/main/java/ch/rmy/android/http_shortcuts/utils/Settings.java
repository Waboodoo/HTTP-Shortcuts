package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

public class Settings {

    public static final String CLICK_BEHAVIOR_RUN = "run";
    public static final String CLICK_BEHAVIOR_EDIT = "edit";
    public static final String CLICK_BEHAVIOR_MENU = "menu";

    public static final String THEME_BLUE = "blue";
    public static final String THEME_GREEN = "green";
    public static final String THEME_ORANGE = "orange";
    public static final String THEME_RED = "red";
    public static final String THEME_GREY = "grey";
    public static final String THEME_PURPLE = "purple";
    public static final String THEME_INDIGO = "indigo";

    private static final String KEY_CLICK_BEHAVIOR = "click_behavior";
    private static final String KEY_IMPORT_EXPORT_DIR = "import_export_dir";
    private static final String KEY_CHANGE_LOG_PERMANENTLY_HIDDEN = "change_log_permanently_hidden";
    private static final String KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_version";
    private static final String KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN = "icon_name_change_permanently_hidden";
    private static final String KEY_THEME = "theme";

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

    public boolean isChangeLogPermanentlyHidden() {
        return preferences.getBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, false);
    }

    public void setChangeLogPermanentlyHidden(boolean hidden) {
        preferences.edit().putBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, hidden).apply();
    }

    public int getChangeLogLastVersion() {
        return preferences.getInt(KEY_CHANGE_LOG_LAST_VERSION, 0);
    }

    public void setChangeLogLastVersion(int version) {
        preferences.edit().putInt(KEY_CHANGE_LOG_LAST_VERSION, version).apply();
    }

    public boolean isIconNameWarningPermanentlyHidden() {
        return preferences.getBoolean(KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN, false);
    }

    public void setIconNameWarningPermanentlyHidden(boolean hidden) {
        preferences.edit().putBoolean(KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN, hidden).apply();
    }

    public String getTheme() {
        return preferences.getString(KEY_THEME, THEME_BLUE);
    }

    public void setTheme(String theme) {
        preferences.edit().putString(KEY_THEME, theme).apply();
    }

}
