package ch.rmy.android.http_shortcuts.data.enums

enum class ConfirmationType(
    val type: String,
) {
    SIMPLE("simple"),
    BIOMETRIC("biometric"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            entries.firstOrNull { it.type == type }
    }
}
