package ch.rmy.android.http_shortcuts.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONArray.toListOfStrings(): List<String> =
    mutableListOf<String>().also { list ->
        for (i in 0..length()) {
            try {
                list.add(getString(i))
            } catch (e: JSONException) {
                // ignore
            }
        }
    }

fun JSONArray.toListOfObjects(): List<JSONObject> =
    mutableListOf<JSONObject>().also { list ->
        for (i in 0..length()) {
            optJSONObject(i)?.let { element ->
                list.add(element)
            }
        }
    }

fun JsonElement.getArrayOrEmpty(name: String): JsonArray =
    takeIf { isJsonObject }
        ?.asJsonObject
        ?.get(name)
        ?.takeIf { it.isJsonArray }?.asJsonArray
        ?: JsonArray()