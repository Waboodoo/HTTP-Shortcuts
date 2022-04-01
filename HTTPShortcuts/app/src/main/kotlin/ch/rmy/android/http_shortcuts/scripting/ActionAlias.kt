package ch.rmy.android.http_shortcuts.scripting

class ActionAlias(
    val functionName: String,
    val functionNameAliases: Set<String> = emptySet(),
    val parameters: Int,
)
