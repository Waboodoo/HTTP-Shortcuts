package ch.rmy.android.http_shortcuts.utils

import android.appwidget.AppWidgetManager
import android.content.Intent
import ch.rmy.android.http_shortcuts.plugin.PluginEditActivity

enum class SelectionMode {

    NORMAL,
    HOME_SCREEN_SHORTCUT_PLACEMENT,
    HOME_SCREEN_WIDGET_PLACEMENT,
    PLUGIN;

    companion object {

        fun determineMode(action: String?) = when (action) {
            Intent.ACTION_CREATE_SHORTCUT -> HOME_SCREEN_SHORTCUT_PLACEMENT
            AppWidgetManager.ACTION_APPWIDGET_CONFIGURE -> HOME_SCREEN_WIDGET_PLACEMENT
            PluginEditActivity.ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> PLUGIN
            else -> NORMAL
        }
    }
}
