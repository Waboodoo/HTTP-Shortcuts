package ch.rmy.android.http_shortcuts.scripting

class ActionAlias(
    val functionName: String,
    val parameters: List<String> = emptyList(),
    val functionNameAliases: Set<String> = emptySet(),
    val returnType: ReturnType = ReturnType.STRING,
) {

    enum class ReturnType {
        STRING,
        BOOLEAN
    }

}