package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable

@Deprecated("Will be removed eventually")
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

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        val headerValue = response?.headers?.get(headerKey)
            ?: volleyError?.networkResponse?.headers?.get(headerKey)
            ?: return Completable.complete()

        variableManager.setVariableValueById(variableId, headerValue)
        return Commons.setVariableValue(variableId, headerValue)
    }

    companion object {

        private const val KEY_HEADER_KEY = "headerKey"
        private const val KEY_VARIABLE_ID = "variableId"

    }

}