package ch.rmy.android.http_shortcuts.extensions

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONArray.toListOfStrings(): List<String> =
    buildList {
        for (i in 0..length()) {
            try {
                add(getString(i))
            } catch (e: JSONException) {
                // ignore
            }
        }
    }

fun JSONArray.toListOfObjects(): List<JSONObject> =
    buildList {
        for (i in 0..length()) {
            optJSONObject(i)?.let { element ->
                add(element)
            }
        }
    }
