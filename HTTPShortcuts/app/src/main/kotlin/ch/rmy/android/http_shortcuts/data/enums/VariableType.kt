package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class VariableType(
    val type: String,
    val supportsDialogTitle: Boolean = false,
    val supportsDialogMessage: Boolean = false,
    val storesValue: Boolean = true,
) {
    CONSTANT("constant"),
    TEXT("text", supportsDialogTitle = true, supportsDialogMessage = true),
    NUMBER("number", supportsDialogTitle = true, supportsDialogMessage = true),
    PASSWORD("password", supportsDialogTitle = true, supportsDialogMessage = true),
    SELECT("select", supportsDialogTitle = true, storesValue = false),
    COLOR("color", supportsDialogTitle = true),
    DATE("date", supportsDialogTitle = true),
    TIME("time", supportsDialogTitle = true),
    SLIDER("slider", supportsDialogTitle = true, supportsDialogMessage = true),
    TOGGLE("toggle"),
    INCREMENT("increment"),
    UUID("uuid", storesValue = false),
    CLIPBOARD("clipboard", storesValue = false),
    TIMESTAMP("timestamp", storesValue = false),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): VariableType? =
            entries.find { it.type == type }
    }
}
