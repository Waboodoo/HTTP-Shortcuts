package ch.rmy.android.http_shortcuts.data.enums

enum class ShortcutTriggerType {

    TEST_IN_EDITOR,
    DIALOG_RERUN,
    WINDOW_RERUN,
    MAIN_SCREEN,
    DEEP_LINK,
    SHARE,
    VOICE,
    PLUGIN,
    BROADCAST,
    SCHEDULE,
    QUICK_SETTINGS_TILE,
    APP_SHORTCUT,
    HOME_SCREEN_SHORTCUT,
    SECONDARY_LAUNCHER_APP,
    LEGACY_SHORTCUT,
    WIDGET,
    SCRIPTING;

    companion object {
        fun parse(name: String?) =
            values().firstOrNull { it.name == name }
    }
}
