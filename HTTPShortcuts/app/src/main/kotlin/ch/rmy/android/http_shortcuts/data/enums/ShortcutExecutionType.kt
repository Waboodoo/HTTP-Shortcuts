package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ShortcutExecutionType(
    val type: String,
) {

    HTTP("app"),
    BROWSER("browser"),
    SCRIPTING("scripting"),
    TRIGGER("trigger"), // AKA "Multi Shortcut"
    MQTT("mqtt"),
    WAKE_ON_LAN("wol"),
    ;

    companion object {
        fun parse(type: String): ShortcutExecutionType? =
            entries.find { it.type == type }
    }
}
