package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ShortcutExecutionType(
    val type: String,
) {

    HTTP(type = "app"),
    BROWSER(type = "browser"),
    SCRIPTING(type = "scripting"),
    TRIGGER(type = "trigger"), // AKA "Multi Shortcut"
    ;

    companion object {

        fun get(type: String) =
            entries.first { it.type == type }
    }
}
