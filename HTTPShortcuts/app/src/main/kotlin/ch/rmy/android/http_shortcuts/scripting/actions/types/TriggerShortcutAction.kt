package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.utils.DateUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable

class TriggerShortcutAction(
    private val shortcutNameOrId: String?,
    private val variableValues: Map<String, Any?>?,
    private val delay: Int?,
) : BaseAction() {

    private val shortcutRepository = ShortcutRepository()
    private val pendingExecutionsRepository = PendingExecutionsRepository()

    override fun execute(executionContext: ExecutionContext): Completable {
        if (executionContext.recursionDepth >= MAX_RECURSION_DEPTH) {
            return Completable
                .error(
                    ActionException {
                        it.getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
                    }
                )
        }
        return shortcutRepository.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
            .flatMapCompletable { shortcut ->
                val delay = delay ?: shortcut.delay
                pendingExecutionsRepository.createPendingExecution(
                    shortcutId = shortcut.id,
                    resolvedVariables = variableValues?.mapValues { it.value?.toString() ?: "" } ?: emptyMap(),
                    tryNumber = 0,
                    waitUntil = DateUtil.calculateDate(delay),
                    requiresNetwork = shortcut.isWaitForNetwork,
                    recursionDepth = if (delay >= 500) 0 else executionContext.recursionDepth + 1,
                )
            }
            .onErrorResumeNext { error ->
                if (error is NoSuchElementException) {
                    Completable
                        .error(
                            ActionException {
                                it.getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
                            }
                        )
                } else {
                    Completable.error(error)
                }
            }
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 5
    }
}
