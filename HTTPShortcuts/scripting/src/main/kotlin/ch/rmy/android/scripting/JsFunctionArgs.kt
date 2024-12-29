package ch.rmy.android.scripting

interface JsFunctionArgs {
    fun getInt(position: Int): Int?

    fun getString(position: Int): String?

    fun getBoolean(position: Int): Boolean?

    fun getObject(position: Int): Map<String, Any?>?

    fun getByteArray(position: Int): ByteArray?

    fun getListOfStrings(position: Int): List<String>?

    fun getListOfObjects(position: Int): List<Map<String, Any?>>?

    fun getJsFunctionArgs(position: Int): JsFunctionArgs?
}

internal class JsFunctionArgsImpl(
    private val data: List<JsValue?>,
) : JsFunctionArgs {
    override fun getInt(position: Int): Int? =
        data.getOrNull(position)?.asInt()

    override fun getBoolean(position: Int): Boolean? =
        data.getOrNull(position)?.asBoolean()

    override fun getString(position: Int): String? =
        data.getOrNull(position)?.asString()

    override fun getObject(position: Int): Map<String, Any?>? =
        data.getOrNull(position)?.asObject()

    override fun getByteArray(position: Int): ByteArray? =
        data.getOrNull(position)?.asByteArray()

    override fun getListOfStrings(position: Int): List<String>? =
        data.getOrNull(position)?.asListOfStrings()

    override fun getListOfObjects(position: Int): List<Map<String, Any?>>? =
        data.getOrNull(position)?.asListOfObjects()

    override fun getJsFunctionArgs(position: Int): JsFunctionArgs? =
        data.getOrNull(position)?.asJsFunctionArgs()
}
