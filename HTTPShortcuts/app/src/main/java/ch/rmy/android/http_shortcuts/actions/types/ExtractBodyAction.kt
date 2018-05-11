package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import org.jdeferred2.Promise
import java.nio.charset.Charset

class ExtractBodyAction(
        id: String,
        actionType: ExtractBodyActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var extractionType: String
        get() = internalData[KEY_EXTRACTION_TYPE] ?: ""
        set(value) {
            internalData[KEY_EXTRACTION_TYPE] = value
        }

    var variableKey: String
        get() = internalData[KEY_VARIABLE_KEY] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_KEY] = value
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

    override fun getDescription(context: Context): CharSequence =
            Variables.rawPlaceholdersToVariableSpans(
                    context,
                    context.getString(R.string.action_type_extract_body_description, Variables.toRawPlaceholder(variableKey))
            )

    override fun perform(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> {
        val body = when {
            response != null -> response.bodyAsString
            volleyError?.networkResponse?.data != null -> volleyError.networkResponse.data.toString(Charset.forName(HttpHeaderParser.parseCharset(volleyError.networkResponse.headers, "UTF-8")))
            else -> return PromiseUtils.resolve(Unit)
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
                    return PromiseUtils.resolve(Unit)
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
                                else -> return PromiseUtils.resolve(Unit)
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
                    return PromiseUtils.resolve(Unit)
                }
            }
            else -> return PromiseUtils.resolve(Unit)
        }

        variableValues[variableKey] = value
        Controller().use { controller ->
            return controller.setVariableValue(variableKey, value)
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            ExtractBodyActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_EXTRACTION_TYPE = "extractionType"
        private const val KEY_VARIABLE_KEY = "variableKey"
        private const val KEY_SUBSTRING_START = "substringStart"
        private const val KEY_SUBSTRING_END = "substringEnd"
        private const val KEY_JSON_PATH = "jsonPath"

        const val EXTRACTION_OPTION_FULL_BODY = "full_body"
        const val EXTRACTION_OPTION_SUBSTRING = "substring"
        const val EXTRACTION_OPTION_PARSE_JSON = "parse_json"

    }

}