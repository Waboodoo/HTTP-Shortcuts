package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.extensions.toPromise
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import org.jdeferred2.Promise

class ExtractStatusCodeAction(
    id: String,
    actionType: ExtractStatusCodeActionType,
    data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var variableId: String
        get() = internalData[KEY_VARIABLE_ID] ?: ""
        set(value) {
            internalData[KEY_VARIABLE_ID] = value
        }

    override fun getDescription(context: Context): CharSequence =
        context.getString(R.string.action_type_extract_status_code_description, Variables.toRawPlaceholder(variableId))

    override fun perform(context: Context, shortcutId: String, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> {
        val statusCode = response?.statusCode ?: volleyError?.networkResponse?.statusCode
        ?: return PromiseUtils.resolve(Unit)
        val statusCodeString = statusCode.toString()
        variableValues[variableId] = statusCodeString
        Controller().use { controller ->
            return controller.setVariableValue(variableId, statusCodeString).toPromise()
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        ExtractStatusCodeActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        private const val KEY_VARIABLE_ID = "variableId"

    }

}