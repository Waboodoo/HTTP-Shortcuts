package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import io.reactivex.Completable

class TriggerShortcutAction(
    actionType: TriggerShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val shortcutNameOrId: String = data[KEY_SHORTCUT_NAME_OR_ID] ?: ""
    private val variableValuesJson: String = data[KEY_VARIABLE_VALUES] ?: ""

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return Completable
                .error(ActionException {
                    it.getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
                })
        }
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId)
            ?: return Completable
                .error(ActionException {
                    it.getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
                })

        return Commons.createPendingExecution(
            shortcutId = shortcut.id,
            resolvedVariables = getVariableValues(variableValuesJson),
            tryNumber = 0,
            waitUntil = DateUtil.calculateDate(shortcut.delay),
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = recursionDepth + 1
        )
    }

    companion object {

        const val KEY_SHORTCUT_NAME_OR_ID = "shortcutId"
        const val KEY_VARIABLE_VALUES = "variables"

        private const val MAX_RECURSION_DEPTH = 5

        private fun getVariableValues(json: String): Map<String, String> =
            try {
                GsonUtil.fromJsonObject<Any?>(json)
                    .mapValues { it.value?.toString() ?: "" }
            } catch (e: Exception) {
                emptyMap<String, String>()
            }

    }

}