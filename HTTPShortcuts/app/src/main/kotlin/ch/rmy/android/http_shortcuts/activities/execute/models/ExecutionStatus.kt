package ch.rmy.android.http_shortcuts.activities.execute.models

import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.http.UploadProgress
import ch.rmy.android.http_shortcuts.variables.ResolvedVariableValues
import java.io.IOException

sealed interface ExecutionStatus {

    data object Preparing : ExecutionStatus

    data class Started(
        override val variableValues: ResolvedVariableValues,
    ) : ExecutionStatus, WithVariables

    data class ProgressUpdate(
        val progress: UploadProgress,
    ) : ExecutionStatus

    data class WrappingUp(
        override val variableValues: ResolvedVariableValues,
        override val result: String?,
    ) : ExecutionStatus, WithVariables, WithResult

    data class CompletedSuccessfully(
        override val response: ShortcutResponse?,
        override val variableValues: ResolvedVariableValues,
        override val result: String?,
    ) : ExecutionStatus, WithResponse, WithVariables, WithResult

    data class CompletedWithError(
        val error: IOException?,
        override val response: ShortcutResponse?,
        override val variableValues: ResolvedVariableValues,
        override val result: String?,
    ) : ExecutionStatus, WithResponse, WithVariables, WithResult

    interface WithResponse {
        val response: ShortcutResponse?
    }

    interface WithVariables {
        val variableValues: ResolvedVariableValues
    }

    interface WithResult {
        val result: String?
    }
}
