package ch.rmy.android.scripting.quickjs

import ch.rmy.android.scripting.JsFunctionArgs
import ch.rmy.android.scripting.JsFunctionArgsImpl
import ch.rmy.android.scripting.JsValue
import ch.rmy.android.scripting.utils.withoutCycles
import com.whl.quickjs.wrapper.QuickJSArray
import com.whl.quickjs.wrapper.QuickJSObject
import org.json.JSONArray
import org.json.JSONObject

internal class QuickJsValueWrapper(
    private val value: Any?,
) : JsValue {
    @OptIn(ExperimentalStdlibApi::class)
    override fun asString(): String? =
        when (value) {
            null -> null
            is String -> value
            is Map<*, *> -> JSONObject((value as Map<Any?, Any?>).withoutCycles()).toString()
            is List<*> -> JSONArray((value as List<Any?>).withoutCycles()).toString()
            is QuickJSObject -> value.stringify()
            is ByteArray -> value.toHexString()
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
        when (value) {
            null -> null
            "" -> ByteArray(0)
            is ByteArray -> value
            is String -> value.toByteArray()
            is List<*> -> ByteArray(value.size) {
                when (val entry = value[it]) {
                    is Int -> entry.toByte()
                    else -> 0
                }
            }
            is QuickJSArray -> {
                value.toArray(null, null, ::createObjectMap)
                    .map { entry ->
                        when (entry) {
                            is Int -> entry.toByte()
                            else -> 0
                        }
                    }
                    .toByteArray()
            }
            is Map<*, *> -> {
                value.entries
                    .map { (_, entry) ->
                        when (entry) {
                            is Int -> entry.toByte()
                            else -> 0
                        }
                    }
                    .toByteArray()
            }
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
