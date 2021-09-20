package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.DateUtil
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Completable

class TriggerShortcutAction(
    private val shortcutNameOrId: String?,
    private val variableValuesJson: String,
    private val delay: Int?,
) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable {
        if (executionContext.recursionDepth >= MAX_RECURSION_DEPTH) {
            return Completable
                .error(ActionException {
                    it.getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
                })
        }
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
            ?: return Completable
                .error(ActionException {
                    it.getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
                })

        val delay = delay ?: shortcut.delay

        return Commons.createPendingExecution(
            shortcutId = shortcut.id,
            resolvedVariables = getVariableValues(variableValuesJson),
            tryNumber = 0,
            waitUntil = DateUtil.calculateDate(delay),
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = if (delay >= 500) 0 else executionContext.recursionDepth + 1,
        )
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 5

        private fun getVariableValues(json: String): Map<String, String> =
            try {
                GsonUtil.fromJsonObject<Any?>(json)
                    .mapValues { it.value?.toString() ?: "" }
            } catch (e: Exception) {
                emptyMap()
            }

    }

}