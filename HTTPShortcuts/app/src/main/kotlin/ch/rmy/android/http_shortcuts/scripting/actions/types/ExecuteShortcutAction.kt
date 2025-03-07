package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionFactory
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.exceptions.UserAbortException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.scripting.ResponseObjectFactory
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.withContext

class ExecuteShortcutAction
@Inject
constructor(
    private val executionFactory: ExecutionFactory,
    private val shortcutRepository: ShortcutRepository,
    private val responseObjectFactory: ResponseObjectFactory,
) : Action<ExecuteShortcutAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject {
        logInfo("Preparing to execute shortcut ($shortcutNameOrId)")
        if (executionContext.recursionDepth >= MAX_RECURSION_DEPTH) {
            logInfo("Not executing shortcut, reached maximum recursion depth")
            throw ActionException {
                getString(R.string.action_type_trigger_shortcut_error_recursion_depth_reached)
            }
        }
        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
        } catch (e: NoSuchElementException) {
            logInfo("Not executing shortcut, not found")
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_triggering, shortcutNameOrId)
            }
        }

        val execution = executionFactory.createExecution(
            ExecutionParams(
                shortcutId = shortcut.id,
                variableValues = executionContext.variableManager.getVariableValuesByIds()
                    .runIfNotNull(variableValues) { overriddenVariableValues ->
                        plus(overriddenVariableValues.mapValues { it.value?.toString().orEmpty() })
                    },
                recursionDepth = executionContext.recursionDepth + 1,
                trigger = ShortcutTriggerType.SCRIPTING,
                isNested = true,
            ),
            dialogHandle = executionContext.dialogHandle,
        )

        val finalStatus = try {
            withContext(Dispatchers.Main) {
                execution.execute()
            }.lastOrNull()
        } catch (e: UserAbortException) {
            if (e.abortAll) {
                throw e
            } else {
                return createResult(
                    executionContext.scriptingEngine,
                    status = "aborted",
                    response = null,
                    error = null,
                    result = null,
                )
            }
        }

        (finalStatus as? ExecutionStatus.WithVariables)?.variableValues?.let {
            executionContext.variableManager.storeVariableValues(it)
        }

        return createResult(
            executionContext.scriptingEngine,
            status = when (finalStatus) {
                is ExecutionStatus.CompletedSuccessfully -> "success"
                is ExecutionStatus.CompletedWithError -> "failure"
                else -> "unknown"
            },
            response = (finalStatus as? ExecutionStatus.WithResponse)
                ?.response
                ?.let { responseObjectFactory.create(executionContext.scriptingEngine, it) },
            error = (finalStatus as? ExecutionStatus.CompletedWithError)
                ?.error
                ?.message,
            result = (finalStatus as? ExecutionStatus.WithResult)
                ?.result,
        )
    }

    data class Params(
        val shortcutNameOrId: ShortcutNameOrId?,
        val variableValues: Map<VariableKey, Any?>?,
    )

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        internal fun VariableManager.storeVariableValues(variableValues: Map<VariableId, String>) {
            variableValues.forEach { (variableId, value) ->
                setVariableValueByKeyOrId(variableId, value)
            }
        }

        internal fun createResult(scriptingEngine: ScriptingEngine, status: String, response: JsObject?, error: String?, result: String?) =
            scriptingEngine.buildJsObject {
                property("status", status)
                property("response", response)
                property("networkError", error)
                property("result", result)
            }
    }
}
