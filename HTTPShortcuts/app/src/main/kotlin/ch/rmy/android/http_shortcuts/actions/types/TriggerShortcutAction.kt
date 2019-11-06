package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import com.android.volley.VolleyError
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class TriggerShortcutAction(
    actionType: TriggerShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var shortcutNameOrId: String
        get() = internalData[KEY_SHORTCUT_NAME_OR_ID] ?: ""
        set(value) {
            internalData[KEY_SHORTCUT_NAME_OR_ID] = value
        }

    val shortcutName: String?
        get() = DataSource.getShortcutByNameOrId(shortcutNameOrId)?.name

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return Completable.fromAction {
                context.showToast(R.string.action_type_trigger_shortcut_error_recursion_depth_reached, long = true)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        }
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId)
        if (shortcut == null) {
            return Completable.fromAction {
                context.showToast(String.format(context.getString(R.string.error_shortcut_not_found_for_triggering), shortcutNameOrId), long = true)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        }
        return Commons.createPendingExecution(
            shortcutId = shortcut.id,
            tryNumber = 0,
            waitUntil = DateUtil.calculateDate(EXECUTION_DELAY),
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = recursionDepth + 1
        )
    }

    companion object {

        const val KEY_SHORTCUT_NAME_OR_ID = "shortcutId"

        private const val MAX_RECURSION_DEPTH = 5

        private const val EXECUTION_DELAY = 300

    }

}