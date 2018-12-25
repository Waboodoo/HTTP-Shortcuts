package ch.rmy.android.http_shortcuts.utils

import android.content.Intent

import ch.rmy.android.http_shortcuts.plugin.PluginEditActivity

enum class SelectionMode {

    NORMAL,
    HOME_SCREEN,
    PLUGIN;

    companion object {

        fun determineMode(action: String?) = when (action) {
            Intent.ACTION_CREATE_SHORTCUT -> HOME_SCREEN
            PluginEditActivity.ACTION_SELECT_SHORTCUT_FOR_PLUGIN -> PLUGIN
            else -> NORMAL
        }

    }

}
