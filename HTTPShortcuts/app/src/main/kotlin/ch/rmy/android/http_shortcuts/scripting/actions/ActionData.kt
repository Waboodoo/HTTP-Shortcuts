package ch.rmy.android.http_shortcuts.scripting.actions

import androidx.core.text.isDigitsOnly
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.liquidplayer.javascript.JSValue

class ActionData(
    private val data: List<JSValue?> = emptyList(),
) {

    fun getString(index: Int): String? {
        val value = data.getOrNull(index)
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray -> JSONArray(value.toJSArray().toArray()).toString()
            value.isObject || value.isArray -> value.toJSON()
            else -> value.toString()
        }
    }

    fun getInt(index: Int): Int? =
        getString(index)?.toIntOrNull()

    fun getBoolean(index: Int): Boolean? =
        getString(index)?.toBoolean()

    fun getObject(index: Int): Map<String, Any?>? {
        val value = data.getOrNull(index)
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

    fun getList(index: Int): List<Any?>? {
        val value = data.getOrNull(index)
        return when {
            value == null || value.isNull || value.isUndefined -> null
            value.isTypedArray || value.isArray -> value.toJSArray()
            else -> null
        }
    }

    fun getByteArray(index: Int): ByteArray? {
        val value = data.getOrNull(index)
        return when {
            value == null -> null
            value.isUint8Array || value.isInt8Array -> value.toJSArray().toArray().map { it.toString().toByte() }.toByteArray()
            value.isString && value.toString().isDigitsOnly() -> ByteArray(1).apply { this[0] = value.toNumber().toInt().toByte() }
            value.isArray -> value.toJSArray().mapNotNull { it.toString().toByte() }.toByteArray()
            else -> getString(index)?.toByteArray()
        }
    }
}
