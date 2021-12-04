package ch.rmy.android.http_shortcuts.scripting.actions

import androidx.core.text.isDigitsOnly
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.liquidplayer.javascript.JSValue

class ActionDTO(
    val type: String,
    val data: Map<String, JSValue?> = emptyMap(),
) {

    fun getString(key: String): String? {
        val value = data[key]
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray -> JSONArray(value.toJSArray().toArray()).toString()
            value.isObject || value.isArray -> value.toJSON()
            else -> value.toString()
        }
    }

    fun getInt(key: String): Int? =
        getString(key)?.toIntOrNull()

    fun getBoolean(key: String): Boolean? =
        getString(key)?.toBoolean()

    fun getObject(key: String): Map<String, Any?>? {
        val value = data[key]
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isObject && !value.isArray -> try {
                JSONObject(value.toJSON())
                    .let { `object` ->
                        mutableMapOf<String, Any?>()
                            .apply {
                                `object`.keys().forEach { key ->
                                    put(key, `object`[key])
                                }
                            }
                    }
            } catch (e: JSONException) {
                null
            }
            else -> null
        }
    }

    fun getList(key: String): List<Any?>? {
        val value = data[key]
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray || value.isArray -> value.toJSArray()
            else -> null
        }
    }

    fun getByteArray(key: String): ByteArray? {
        val value = data[key]
        return when {
            value == null -> null
            value.isUint8Array || value.isInt8Array -> value.toJSArray().toArray().map { it.toString().toByte() }.toByteArray()
            value.isString && value.toString().isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toNumber().toInt().toByte() }
            else -> getString(key)?.toByteArray()
        }
    }
}
