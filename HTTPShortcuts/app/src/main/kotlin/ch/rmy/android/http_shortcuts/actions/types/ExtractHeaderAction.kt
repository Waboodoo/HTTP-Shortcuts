package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable

class ExtractHeaderAction(
    actionType: ExtractHeaderActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var headerKey: String
        get() = internalData[KEY_HEADER_KEY] ?: ""
        set(value) {
            internalData[KEY_HEADER_KEY] = value
        }

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun getDescription(context: Context): CharSequence =
        context.getString(R.string.action_type_extract_header_description, headerKey, Variables.toRawPlaceholder(variableId))

    override fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val headerValue = response?.headers?.get(headerKey)
            ?: volleyError?.networkResponse?.headers?.get(headerKey)
            ?: return Completable.complete()

        variableValues[variableId] = headerValue
        return Commons.setVariableValue(variableId, headerValue)
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        ExtractHeaderActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_HEADER_KEY = "headerKey"
        private const val KEY_VARIABLE_ID = "variableId"

    }

}