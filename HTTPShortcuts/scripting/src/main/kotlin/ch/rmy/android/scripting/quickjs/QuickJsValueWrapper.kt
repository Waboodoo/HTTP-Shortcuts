package ch.rmy.android.scripting.quickjs

import androidx.core.text.isDigitsOnly
import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsValue
import com.whl.quickjs.wrapper.QuickJSArray
import com.whl.quickjs.wrapper.QuickJSObject
import org.json.JSONArray
import org.json.JSONObject

internal class QuickJsValueWrapper(
    private val value: Any?,
) : JsValue {
    override fun asString(): String? =
        when (value) {
            null -> null
            is String -> value
            is Map<*, *> -> JSONObject(value).toString()
            is List<*> -> JSONArray(value).toString()
            is QuickJSObject -> value.stringify()
            else -> value.toString()
        }

    override fun asObject(): Map<String, Any?>? =
        when (value) {
            is Map<*, *> -> value.mapKeysToString()
            is QuickJSObject -> value.toMap(null, null, ::createObjectMap)
            else -> null
        }

    private fun Map<*, *>.mapKeysToString() =
        mapKeys { (key, _) -> key.toString() }

    override fun asByteArray(): ByteArray? =
        when {
            value == null -> null
            value == "" -> ByteArray(0)
            value is ByteArray -> value
            value is String && value.isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toInt().toByte() }
            value is String -> value.toByteArray()
            else -> value.toString().toByteArray()
        }

    override fun asListOfStrings(): List<String>? =
        asListOfAny()?.map { it.toString() }

    override fun asListOfObjects(): List<Map<String, Any?>>? =
        asListOfAny()
            ?.map {
                ((it as? Map<*, *>)?.mapKeysToString()) ?: emptyMap()
            }

    private fun asListOfAny(): List<Any?>? =
        when (value) {
            is List<*> -> value
            is QuickJSArray -> value.toArray(null, null, ::createObjectMap)
            else -> null
        }

    private fun createObjectMap(): MutableMap<String, Any?> =
        mutableMapOf()

    override fun asJsFunctionArgs(): JsFunctionArgs? =
        (asListOfAny() ?: asObject()?.values)
            ?.map(::QuickJsValueWrapper)
            ?.let(::JsFunctionArgsImpl)
}
