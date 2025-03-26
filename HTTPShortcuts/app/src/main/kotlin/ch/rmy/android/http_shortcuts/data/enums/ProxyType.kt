package ch.rmy.android.http_shortcuts.data.enums

enum class ProxyType(
    val type: String,
    val supportsAuthentication: Boolean,
) {

    HTTP("HTTP", supportsAuthentication = false),
    SOCKS("SOCKS", supportsAuthentication = true),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): ProxyType? =
            entries.find { it.type == type }
    }
}
