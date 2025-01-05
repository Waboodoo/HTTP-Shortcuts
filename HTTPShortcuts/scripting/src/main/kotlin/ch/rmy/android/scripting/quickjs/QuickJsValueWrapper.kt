package ch.rmy.android.scripting.quickjs

import androidx.core.text.isDigitsOnly
import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsValue
import com.whl.quickjs.wrapper.QuickJSArray
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
            else -> value.toString()
        }

    override fun asObject(): Map<String, Any?>? =
        (value as? Map<String, Any?>)

    override fun asByteArray(): ByteArray? =
        when {
            value == null -> null
            value is ByteArray -> value
            value is String && value.isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toInt().toByte() }
            value is String -> value.toByteArray()
            else -> value.toString().toByteArray()
        }

    override fun asListOfStrings(): List<String>? =
        (value as? ArrayList<*>)?.map { it.toString() }

    override fun asListOfObjects(): List<Map<String, Any?>>? =
        (value as? ArrayList<*>)
            ?.map {
                (it as? Map<String, Any?>) ?: emptyMap()
            }

    override fun asJsFunctionArgs(): JsFunctionArgs? =
        (value as? QuickJSArray)
            ?.toArray()
            ?.map {
                QuickJsValueWrapper(it)
            }
            ?.let(::JsFunctionArgsImpl)
}
