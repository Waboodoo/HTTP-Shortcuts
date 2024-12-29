package ch.rmy.android.scripting.liquidcore

import androidx.core.text.isDigitsOnly
import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsValue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.liquidplayer.javascript.JSValue

internal class LiquidCoreJSValueWrapper(
    private val value: JSValue?,
) : JsValue {
    override fun asString(): String? =
        when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray -> JSONArray(value.toJSArray().toArray()).toString()
            value.isObject || value.isArray -> value.toJSON()
            else -> value.toString()
        }

    override fun asObject(): Map<String, Any?>? =
        value.toMap()

    private fun JSValue?.toMap(): Map<String, Any?>? {
        val self = this
        return when {
            self == null || self.isNull || self.isUndefined -> null
            self.isObject && !self.isArray -> try {
                buildMap<String, Any?> {
                    JSONObject(self.toJSON())
                        .let { `object` ->
                            `object`.keys().forEach { key ->
                                put(key, `object`[key])
                            }
                        }
                }
            } catch (_: JSONException) {
                null
            }
            else -> null
        }
    }

    override fun asByteArray(): ByteArray? =
        when {
            value == null -> null
            value.isUint8Array || value.isInt8Array -> value.toJSArray().toArray().map { it.toString().toByte() }.toByteArray()
            value.isString && value.toString().isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toNumber().toInt().toByte() }
            value.isArray -> value.toJSArray().mapNotNull { it.toString().toByte() }.toByteArray()
            else -> asString()?.toByteArray()
        }

    override fun asListOfStrings(): List<String>? =
        asList()?.map { it?.toString() ?: "" }

    override fun asListOfObjects(): List<Map<String, Any?>>? =
        asList()?.map { (it as? JSValue)?.toMap() ?: emptyMap() }

    override fun asJsFunctionArgs(): JsFunctionArgs? =
        value
            ?.toJSArray()
            ?.toList()
            ?.map { (it as? JSValue)?.let { LiquidCoreJSValueWrapper(it) } }
            ?.let {
                JsFunctionArgsImpl(it)
            }

    private fun asList(): List<Any?>? {
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray || value.isArray -> value.toJSArray()
            else -> null
        }
    }
}
