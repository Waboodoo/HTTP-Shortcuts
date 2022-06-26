package ch.rmy.android.http_shortcuts.data.enums

enum class VariableType(
    val type: String,
    val supportsDialogTitle: Boolean = false,
    val supportsDialogMessage: Boolean = false,
) {
    CONSTANT("constant"),
    TEXT("text", supportsDialogTitle = true, supportsDialogMessage = true),
    NUMBER("number", supportsDialogTitle = true, supportsDialogMessage = true),
    PASSWORD("password", supportsDialogTitle = true, supportsDialogMessage = true),
    SELECT("select", supportsDialogTitle = true),
    TOGGLE("toggle"),
    COLOR("color", supportsDialogTitle = true),
    DATE("date", supportsDialogTitle = true),
    TIME("time", supportsDialogTitle = true),
    SLIDER("slider", supportsDialogTitle = true, supportsDialogMessage = true);

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values()
                .firstOrNull { it.type == type }
                ?: CONSTANT
    }
}
