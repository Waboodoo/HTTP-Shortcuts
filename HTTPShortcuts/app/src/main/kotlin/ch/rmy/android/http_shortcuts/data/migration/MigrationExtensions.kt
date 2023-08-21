package ch.rmy.android.http_shortcuts.data.migration

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.realm.kotlin.dynamic.DynamicRealmObject
import io.realm.kotlin.dynamic.getNullableValue
import io.realm.kotlin.dynamic.getValue

fun DynamicRealmObject.getString(key: String): String? =
    try {
        getValue(key)
    } catch (e: IllegalArgumentException) {
        try {
            getNullableValue(key)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

fun JsonObject.getArray(key: String): JsonArray =
    get(key)?.asJsonArray ?: JsonArray()

fun JsonObject.getObjectArray(key: String): Iterable<JsonObject> =
    get(key)?.asJsonArray?.mapNotNull { it?.takeIf { it.isJsonObject }?.asJsonObject } ?: emptyList()

fun JsonObject.getString(key: String): String? =
    get(key)?.takeIf { it.isJsonPrimitive }?.asString

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
