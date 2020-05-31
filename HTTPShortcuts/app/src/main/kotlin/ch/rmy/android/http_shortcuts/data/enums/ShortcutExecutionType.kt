package ch.rmy.android.http_shortcuts.data.enums

enum class ShortcutExecutionType(
    val type: String,
    val usesUrl: Boolean = false,
    val usesRequestOptions: Boolean = false,
    val usesResponse: Boolean = false,
    val usesScriptingEditor: Boolean = true
) {

    APP(type = "app", usesUrl = true, usesRequestOptions = true, usesResponse = true),
    BROWSER(type = "browser", usesUrl = true),
    SCRIPTING(type = "scripting"),
    TRIGGER(type = "trigger", usesScriptingEditor = false);

    companion object {

        fun get(type: String) =
            ShortcutExecutionType.values().first { it.type == type }

    }

}