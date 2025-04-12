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
    SCHEDULE,
    SCHEDULE_IMMEDIATELY,
    REPETITION,
    QUICK_SETTINGS_TILE,
    APP_SHORTCUT,
    HOME_SCREEN_SHORTCUT,
    SECONDARY_LAUNCHER_APP,
    LEGACY_SHORTCUT,
    WIDGET,
    SCRIPTING,
    QUICK_ACCESS_DEVICE_CONTROLS,
    ;

    companion object {
        fun parse(name: String): ShortcutTriggerType? =
            entries.find { it.name == name }
    }
}
