package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.exceptions.JavaScriptException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class SelectionAction
@Inject
constructor() : Action<SelectionAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? {
        val options = parseData(dataObject, dataList)
        if (options.isEmpty()) {
            return null
        }

        return try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.Selection(
                    values = options.entries.map { (key, value) -> key to value },
                )
            )
        } catch (e: DialogCancellationException) {
            null
        }
    }

    private fun parseData(dataObject: Map<String, Any?>?, dataList: List<Any?>?): Map<String, String> =
        dataObject?.mapValues { it.value?.toString() ?: "" }
            ?: dataList?.map { it?.toString() ?: "" }?.associateWith { it }
            ?: throw JavaScriptException("showSelection function expects object or array as argument")

    data class Params(
        val dataObject: Map<String, Any?>?,
        val dataList: List<Any?>?,
    )
}
