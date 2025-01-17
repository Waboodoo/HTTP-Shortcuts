package ch.rmy.android.scripting.quickjs

import androidx.core.text.isDigitsOnly
import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsValue
import com.whl.quickjs.wrapper.JSArray
import com.whl.quickjs.wrapper.JSFunction
import com.whl.quickjs.wrapper.JSObject
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
        value?.asMap()

    private fun Any.asMap(): Map<String, Any?>? =
        when (this) {
            is Map<*, *> -> this.mapKeys { it.key.toString() }
            is QuickJSObject -> this.asMap()
            else -> null
        }

    private fun QuickJSObject.asMap(): Map<String, Any?>? {
        // Using a custom implementation here instead of `toMap()`, as `toMap()` uses a HashMap which does not preserve the insertion order.
        // N.B. that this custom implementation only preserves the order at the object's root level, not in its nested objects.
        // It also doesn't support cyclic structures, but I hope my users don't do crazy stuff like that.
        val jsObject = this
        if (jsObject is JSArray) {
            return null
        }
        val array = names
        try {
            return buildMap<String, Any?> {
                for (i in 0 until array.length()) {
                    val name = array.get(i) as String
                    val value = jsObject.getProperty(name)
                    when (value) {
                        is JSFunction -> Unit
                        is JSArray -> {
                            put(name, value.toArray())
                            value.release()
                        }
                        is JSObject -> {
                            put(name, value.toMap())
                            value.release()
                        }
                        else -> put(name, value)
                    }
                }
            }
        } finally {
            array.release()
        }
    }

    override fun asByteArray(): ByteArray? =
        when {
            value == null -> null
            value is ByteArray -> value
            value is String && value.isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toInt().toByte() }
            value is String -> value.toByteArray()
            else -> value.toString().toByteArray()
        }

    override fun asListOfStrings(): List<String>? =
        getValueAsList()
            ?.map { it.toString() }

    override fun asListOfObjects(): List<Map<String, Any?>>? =
        getValueAsList()
            ?.map {
                it?.asMap() ?: emptyMap()
            }

    private fun getValueAsList(): List<Any?>? =
        when (value) {
            is List<*> -> value
            is QuickJSArray -> buildList<Any?> {
                for (i in 0 until value.length()) {
                    add(value.get(i))
                }
            }
            else -> null
        }

    override fun asJsFunctionArgs(): JsFunctionArgs? =
        (getValueAsList() ?: asObject()?.values)
            ?.map(::QuickJsValueWrapper)
            ?.let(::JsFunctionArgsImpl)
}
