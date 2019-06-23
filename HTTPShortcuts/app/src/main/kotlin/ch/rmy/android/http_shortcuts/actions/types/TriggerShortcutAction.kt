package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.android.volley.VolleyError
import io.reactivex.Completable
import java.util.concurrent.TimeUnit

class TriggerShortcutAction(
    actionType: TriggerShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var shortcutId: String
        get() = internalData[KEY_SHORTCUT_ID] ?: ""
        set(value) {
            internalData[KEY_SHORTCUT_ID] = value
        }

    val shortcutName: String?
        get() {
            Controller().use { controller ->
                return controller.getShortcutById(shortcutId)?.name
            }
        }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            context.showToast(R.string.action_type_trigger_shortcut_error_recursion_depth_reached, long = true)
            return Completable.complete()
        }
        return Completable.complete()
            .delay(EXECUTION_DELAY, TimeUnit.MILLISECONDS)
            .andThen {
                ExecuteActivity.IntentBuilder(context, this.shortcutId)
                    .recursionDepth(recursionDepth + 1)
                    .build()
                    .startActivity(context)
            }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
        TriggerShortcutActionEditorView(context, this)

    companion object {

        const val KEY_SHORTCUT_ID = "shortcutId"

        private const val MAX_RECURSION_DEPTH = 5

        private const val EXECUTION_DELAY = 500L

    }

}