package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class EnqueueShortcutAction
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
) : Action<EnqueueShortcutAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        logInfo("Preparing to enqueue shortcut ($shortcutNameOrId)")
        if (executionContext.recursionDepth >= MAX_RECURSION_DEPTH) {
            logInfo("Not enqueueing shortcut, reached maximum recursion depth")
            throw ActionException {
                getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
            }
        }

        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
        } catch (e: NoSuchElementException) {
            logInfo("Not enqueueing shortcut, not found")
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
            }
        }
        var delay = delay ?: 0
        var recursionDepth = if (delay >= 500) 0 else executionContext.recursionDepth + 1
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            delay = 5000
            recursionDepth = 0
        }

        pendingExecutionsRepository.createPendingExecution(
            shortcutId = shortcut.id,
            resolvedVariables = variableValues?.mapValues { it.value?.toString() ?: "" } ?: emptyMap(),
            tryNumber = 0,
            delay = delay.milliseconds,
            requiresNetwork = shortcut.isWaitForNetwork,
            recursionDepth = recursionDepth,
            type = PendingExecutionType.EXPLICITLY_SCHEDULED,
        )
    }

    data class Params(
        val shortcutNameOrId: ShortcutNameOrId?,
        val variableValues: Map<VariableKey, Any?>?,
        val delay: Int?,
    )

    companion object {

        private const val MAX_RECURSION_DEPTH = 10
    }
}
