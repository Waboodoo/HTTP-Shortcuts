package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import io.reactivex.Completable
import java.nio.charset.Charset

@Deprecated("Will be removed eventually")
class ExtractBodyAction(
    actionType: ExtractBodyActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var extractionType: String
        get() = internalData[KEY_EXTRACTION_TYPE] ?: ""
        set(value) {
            internalData[KEY_EXTRACTION_TYPE] = value
        }

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    var substringStart: Int
        get() = internalData[KEY_SUBSTRING_START]?.toIntOrNull() ?: 0
        set(value) {
            internalData[KEY_SUBSTRING_START] = value.toString()
        }

    var substringEnd: Int
        get() = internalData[KEY_SUBSTRING_END]?.toIntOrNull() ?: 0
        set(value) {
            internalData[KEY_SUBSTRING_END] = value.toString()
        }

    var jsonPath: String
        get() = internalData[KEY_JSON_PATH] ?: ""
        set(value) {
            internalData[KEY_JSON_PATH] = value
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val body = when {
            response != null -> response.bodyAsString
            volleyError?.networkResponse?.data != null -> volleyError.networkResponse.data.toString(Charset.forName(HttpHeaderParser.parseCharset(volleyError.networkResponse.headers, "UTF-8")))
            else -> return Completable.complete()
        }

        val value = when (extractionType) {
            EXTRACTION_OPTION_FULL_BODY -> body
            EXTRACTION_OPTION_SUBSTRING -> {
                var start = substringStart
                if (start < 0) {
                    start += body.length
                }
                var end = substringEnd
                if (end <= 0) {
                    end += body.length
                }
                if (start > end) {
                    return Completable.complete()
                }
                body.substring(start, end)
            }
            EXTRACTION_OPTION_PARSE_JSON -> {
                try {
                    if (jsonPath.isEmpty()) {
                        body
                    } else {
                        val pathParts = jsonPath.split('.')
                        var json = GsonUtil.parseString(body)
                        for (pathPart in pathParts) {
                            json = when {
                                json.isJsonArray -> json.asJsonArray[pathPart.toInt()]
                                json.isJsonObject -> json.asJsonObject.get(pathPart)
                                else -> return Completable.complete()
                            }
                        }

                        when {
                            json.isJsonPrimitive -> {
                                val jsonPrimitive = json.asJsonPrimitive
                                when {
                                    jsonPrimitive.isString -> jsonPrimitive.asString
                                    else -> jsonPrimitive.toString()
                                }
                            }
                            json.isJsonNull -> "null"
                            else -> json.toString()
                        }
                    }
                } catch (e: Exception) {
                    return Completable.complete()
                }
            }
            else -> return Completable.complete()
        }

        variableManager.setVariableValueById(variableId, value)
        return Commons.setVariableValue(variableId, value)
    }

    companion object {

        private const val KEY_EXTRACTION_TYPE = "extractionType"
        private const val KEY_VARIABLE_ID = "variableId"
        private const val KEY_SUBSTRING_START = "substringStart"
        private const val KEY_SUBSTRING_END = "substringEnd"
        private const val KEY_JSON_PATH = "jsonPath"

        const val EXTRACTION_OPTION_FULL_BODY = "full_body"
        const val EXTRACTION_OPTION_SUBSTRING = "substring"
        const val EXTRACTION_OPTION_PARSE_JSON = "parse_json"

    }

}