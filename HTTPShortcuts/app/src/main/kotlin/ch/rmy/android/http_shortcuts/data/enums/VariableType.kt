package ch.rmy.android.http_shortcuts.data.enums

enum class VariableType(val type: String, val hasDialogTitle: Boolean = false) {
    CONSTANT("constant"),
    TEXT("text", hasDialogTitle = true),
    NUMBER("number"),
    PASSWORD("password"),
    SELECT("select", hasDialogTitle = true),
    TOGGLE("toggle"),
    COLOR("color"),
    DATE("date"),
    TIME("time"),
    SLIDER("slider", hasDialogTitle = true);

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values()
                .firstOrNull { it.type == type }
                ?: CONSTANT
    }
}
