package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.utils.DateUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class EnqueueShortcutAction(
    private val shortcutNameOrId: ShortcutNameOrId?,
    private val variableValues: Map<VariableKey, Any?>?,
    private val delay: Int?,
) : BaseAction() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        if (executionContext.recursionDepth >= MAX_RECURSION_DEPTH) {
            throw ActionException {
                it.getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
            }
        }

        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                it.getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
            }
        }
        val delay = delay ?: 0
        pendingExecutionsRepository.createPendingExecution(
            shortcutId = shortcut.id,
            resolvedVariables = variableValues?.mapValues { it.value?.toString() ?: "" } ?: emptyMap(),
            tryNumber = 0,
            waitUntil = DateUtil.calculateDate(delay),
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = if (delay >= 500) 0 else executionContext.recursionDepth + 1,
        )
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 5
    }
}
