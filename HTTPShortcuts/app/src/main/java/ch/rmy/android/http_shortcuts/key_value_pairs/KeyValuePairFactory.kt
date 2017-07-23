package ch.rmy.android.http_shortcuts.key_value_pairs

interface KeyValuePairFactory<out T : KeyValuePair> {

    fun create(key: String, value: String): T

}
