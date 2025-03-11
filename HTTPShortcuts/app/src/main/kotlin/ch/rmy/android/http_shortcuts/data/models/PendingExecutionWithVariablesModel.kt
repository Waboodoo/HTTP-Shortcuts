package ch.rmy.android.http_shortcuts.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class PendingExecutionWithVariablesModel(
    @Embedded val pendingExecution: PendingExecutionModel,
    @Relation(
        parentColumn = "id",
        entityColumn = "pending_execution_id",
    )
    val resolvedVariables: List<ResolvedVariableModel>,
)
