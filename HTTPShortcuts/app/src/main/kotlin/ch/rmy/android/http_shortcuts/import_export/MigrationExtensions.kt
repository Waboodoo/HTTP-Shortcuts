package ch.rmy.android.http_shortcuts.import_export

import com.google.gson.JsonArray
import com.google.gson.JsonObject

fun JsonObject.getArray(key: String): JsonArray =
    get(key)?.asJsonArray ?: JsonArray()

fun JsonObject.getObjectArray(key: String): Iterable<JsonObject> =
    get(key)?.asJsonArray?.mapNotNull { it?.takeIf { it.isJsonObject }?.asJsonObject } ?: emptyList()

fun JsonObject.getString(key: String): String? =
    get(key)?.takeIf { it.isJsonPrimitive }?.asString

fun JsonObject.getInt(key: String): Int? =
    get(key)?.takeIf { it.isJsonPrimitive }?.asInt

fun JsonObject.getObject(key: String): JsonObject? =
    get(key)?.takeIf { it.isJsonObject }?.asJsonObject

fun JsonObject.getOrCreateObject(key: String): JsonObject =
    getObject(key)
        ?: run {
            JsonObject()
                .also { newObject ->
                    add(key, newObject)
                }
        }
