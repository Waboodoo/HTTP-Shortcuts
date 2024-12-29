package ch.rmy.android.scripting

interface JsValue {
    fun asString(): String?

    fun asInt(): Int? =
        asString()?.toIntOrNull()

    fun asBoolean(): Boolean? =
        asString()?.toBoolean()

    fun asObject(): Map<String, Any?>?

    fun asByteArray(): ByteArray?

    fun asListOfStrings(): List<String>?

    fun asListOfObjects(): List<Map<String, Any?>>?

    fun asJsFunctionArgs(): JsFunctionArgs?
}
