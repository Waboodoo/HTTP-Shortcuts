package ch.rmy.android.http_shortcuts.scripting.actions

data class ActionDTO(
    val type: String,
    val data: Map<String, String> = emptyMap(),
) {

    operator fun get(key: String) = data[key]

}
