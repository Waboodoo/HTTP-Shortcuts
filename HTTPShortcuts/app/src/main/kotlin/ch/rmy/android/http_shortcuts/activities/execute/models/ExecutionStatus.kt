package ch.rmy.android.http_shortcuts.activities.execute.models

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.http.ShortcutResponse

sealed interface ExecutionStatus {
    object Preparing : ExecutionStatus

    object InProgress : ExecutionStatus

    object WrappingUp : ExecutionStatus

    data class CompletedSuccessfully(
        val response: ShortcutResponse?,
        val variableValues: Map<VariableId, String>,
    ) : ExecutionStatus

    data class CompletedWithError(
        val error: Exception,
        val response: ShortcutResponse?,
        val variableValues: Map<VariableId, String>,
    ) : ExecutionStatus
}
