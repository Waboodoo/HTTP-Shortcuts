package ch.rmy.android.http_shortcuts.data.enums

enum class VariableType(
    val type: String,
    val supportsDialogTitle: Boolean = false,
    val supportsDialogMessage: Boolean = false,
    val hasFragment: Boolean = false,
) {
    CONSTANT("constant", hasFragment = true),
    TEXT("text", supportsDialogTitle = true, supportsDialogMessage = true, hasFragment = true),
    NUMBER("number", supportsDialogTitle = true, supportsDialogMessage = true, hasFragment = true),
    PASSWORD("password", supportsDialogTitle = true, supportsDialogMessage = true, hasFragment = true),
    SELECT("select", supportsDialogTitle = true, hasFragment = true),
    COLOR("color", supportsDialogTitle = true, hasFragment = true),
    DATE("date", supportsDialogTitle = true, hasFragment = true),
    TIME("time", supportsDialogTitle = true, hasFragment = true),
    SLIDER("slider", supportsDialogTitle = true, supportsDialogMessage = true, hasFragment = true),
    TOGGLE("toggle", hasFragment = true),
    UUID("uuid"),
    CLIPBOARD("clipboard"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values()
                .firstOrNull { it.type == type }
                ?: CONSTANT
    }
}
