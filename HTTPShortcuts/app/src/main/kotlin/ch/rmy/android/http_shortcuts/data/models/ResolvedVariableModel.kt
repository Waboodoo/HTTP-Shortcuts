package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

@Entity(tableName = "resolved_variable")
data class ResolvedVariableModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "pending_execution_id")
    val pendingExecutionId: ExecutionId,
    @ColumnInfo(name = "key")
    val key: VariableKey,
    @ColumnInfo(name = "value")
    val value: String,
)
