package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ShortcutExecutionType(
    val type: String,
    val usesUrl: Boolean = false,
    val requiresHttpUrl: Boolean = false,
    val usesRequestOptions: Boolean = false,
    val usesResponse: Boolean = false,
    val usesScriptingEditor: Boolean = true,
) {

    APP(type = "app", usesUrl = true, requiresHttpUrl = true, usesRequestOptions = true, usesResponse = true),
    BROWSER(type = "browser", usesUrl = true, requiresHttpUrl = false),
    SCRIPTING(type = "scripting"),
    TRIGGER(type = "trigger", usesScriptingEditor = false);

    companion object {

        fun get(type: String) =
            entries.first { it.type == type }
    }
}
