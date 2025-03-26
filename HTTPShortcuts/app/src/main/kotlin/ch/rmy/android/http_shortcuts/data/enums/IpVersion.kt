package ch.rmy.android.http_shortcuts.data.enums

enum class IpVersion(val version: String) {
    V4("4"),
    V6("6"),
    ;

    override fun toString() =
        version

    companion object {
        fun parse(version: String): IpVersion? =
            entries.find { it.version == version }
    }
}
