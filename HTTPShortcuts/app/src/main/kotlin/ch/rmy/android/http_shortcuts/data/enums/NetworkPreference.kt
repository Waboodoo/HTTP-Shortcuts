package ch.rmy.android.http_shortcuts.data.enums

enum class NetworkPreference(val key: String) {
    PREFER_CELLULAR("prefer_cellular"),
    ONLY_CELLULAR("only_cellular"),
    PREFER_WIFI("prefer_wifi"),
    ONLY_WIFI("only_wifi"),
    ;

    companion object {
        fun parse(key: String): NetworkPreference? =
            entries.find { it.key == key }
    }
}
