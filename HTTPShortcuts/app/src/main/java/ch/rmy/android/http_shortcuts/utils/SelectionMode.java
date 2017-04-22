package ch.rmy.android.http_shortcuts.utils;

import android.content.Intent;

import ch.rmy.android.http_shortcuts.plugin.PluginEditActivity;

public enum SelectionMode {

    NORMAL,
    HOME_SCREEN,
    PLUGIN;

    public static SelectionMode determineMode(Intent intent) {
        if (Intent.ACTION_CREATE_SHORTCUT.equals(intent.getAction())) {
            return HOME_SCREEN;
        }
        if (PluginEditActivity.ACTION_SELECT_SHORTCUT_FOR_PLUGIN.equals(intent.getAction())) {
            return PLUGIN;
        }
        return NORMAL;
    }

}
