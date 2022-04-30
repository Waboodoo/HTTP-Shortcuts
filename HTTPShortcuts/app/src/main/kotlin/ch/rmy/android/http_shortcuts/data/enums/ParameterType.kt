package ch.rmy.android.http_shortcuts.data.enums

enum class ParameterType(val type: String) {
    STRING("string"),
    FILE("file"),
    FILES("files");

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values().firstOrNull { it.type == type }
                ?: STRING
    }
}
