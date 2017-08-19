package ch.rmy.android.http_shortcuts.key_value_pairs

interface KeyValuePairFactory<out T : KeyValuePair> { // TODO: Use lambda instead

    fun create(key: String, value: String): T

}
