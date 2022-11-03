package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionFactory
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.scripting.ResponseObjectFactory
import ch.rmy.android.http_shortcuts.utils.ErrorFormatter
import ch.rmy.android.http_shortcuts.variables.VariableManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.withContext
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSObject
import javax.inject.Inject

class ExecuteShortcutAction(
    private val shortcutNameOrId: ShortcutNameOrId?,
    private val variableValues: Map<VariableKey, Any?>?,
) : BaseAction() {

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var errorFormatter: ErrorFormatter

    @Inject
    lateinit var responseObjectFactory: ResponseObjectFactory

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): JSObject {
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

        val execution = executionFactory.createExecution(
            ExecutionParams(
                shortcutId = shortcut.id,
                variableValues = executionContext.variableManager.getVariableValuesByIds()
                    .runIfNotNull(variableValues) { overriddenVariableValues ->
                        plus(overriddenVariableValues.mapValues { it.value?.toString().orEmpty() })
                    },
                recursionDepth = executionContext.recursionDepth + 1,
                isNested = true,
            )
        )

        val finalStatus = withContext(Dispatchers.Main) {
            execution.execute()
        }.lastOrNull()

        (finalStatus as? ExecutionStatus.WithVariables)?.variableValues?.let {
            executionContext.variableManager.storeVariableValues(it)
        }

        return createResult(
            executionContext.jsContext,
            status = when (finalStatus) {
                is ExecutionStatus.CompletedSuccessfully -> "success"
                is ExecutionStatus.CompletedWithError -> "failure"
                else -> "unknown"
            },
            response = (finalStatus as? ExecutionStatus.WithResponse)
                ?.response
                ?.let { responseObjectFactory.create(executionContext.jsContext, it) },
            error = (finalStatus as? ExecutionStatus.CompletedWithError)
                ?.error
                ?.message,
        )
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        private fun VariableManager.storeVariableValues(variableValues: Map<VariableId, String>) {
            variableValues.forEach { (variableId, value) ->
                setVariableValueByKeyOrId(variableId, value)
            }
        }

        private fun createResult(jsContext: JSContext, status: String, response: JSObject? = null, error: String? = null) =
            JSObject(jsContext).apply {
                property("status", status)
                property("response", response)
                property("networkError", error)
            }
    }
}
