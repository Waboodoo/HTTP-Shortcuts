package ch.rmy.android.http_shortcuts.data.domains.pending_executions

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionWithVariablesModel
import ch.rmy.android.http_shortcuts.data.models.ResolvedVariableModel
import kotlin.collections.forEach
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PendingExecutionDao {
    @Transaction
    @Query("SELECT * FROM pending_execution WHERE id = :id")
    abstract suspend fun getPendingExecution(id: ExecutionId): List<PendingExecutionWithVariablesModel>

    @Transaction
    @Query("SELECT * FROM pending_execution")
    abstract fun observePendingExecutions(): Flow<List<PendingExecutionWithVariablesModel>>

    @Transaction
    @Query("SELECT * FROM pending_execution WHERE shortcut_id = :shortcutId")
    abstract suspend fun getPendingExecutionsForShortcut(shortcutId: ShortcutId): List<PendingExecutionWithVariablesModel>

    @Transaction
    @Query("SELECT * FROM pending_execution ORDER BY enqueued_at ASC LIMIT 1")
    abstract suspend fun getNextPendingExecution(): PendingExecutionWithVariablesModel?

    @Transaction
    @Query("SELECT * FROM pending_execution WHERE wait_for_network = 1 ORDER BY enqueued_at ASC LIMIT 1")
    abstract suspend fun getNextPendingExecutionWaitingForNetwork(): PendingExecutionWithVariablesModel?

    @Transaction
    open suspend fun insert(pendingExecution: PendingExecutionModel, resolvedVariables: Map<VariableKey, String>) {
        val pendingExecutionId = insertPendingExecution(pendingExecution).toInt()
        insertResolvedVariables(
            resolvedVariables.entries.map { (key, value) ->
                ResolvedVariableModel(
                    pendingExecutionId = pendingExecutionId,
                    key = key,
                    value = value,
                )
            },
        )
    }

    @Insert
    protected abstract suspend fun insertPendingExecution(pendingExecution: PendingExecutionModel): Long

    @Insert
    protected abstract suspend fun insertResolvedVariables(resolvedVariableModel: List<ResolvedVariableModel>)

    @Transaction
    open suspend fun delete(id: ExecutionId) {
        deleteExecution(id)
        deleteResolvedVariables(id)
    }

    @Query("DELETE FROM pending_execution WHERE id = :id")
    protected abstract suspend fun deleteExecution(id: ExecutionId)

    @Query("DELETE FROM resolved_variable WHERE pending_execution_id = :id")
    protected abstract suspend fun deleteResolvedVariables(id: ExecutionId)

    @Transaction
    open suspend fun deleteForShortcut(shortcutId: ShortcutId) {
        getPendingExecutionsForShortcutForDeletion(shortcutId)
            .forEach {
                deleteExecution(it.id)
            }
    }

    @Query("SELECT * FROM pending_execution WHERE shortcut_id = :shortcutId")
    protected abstract suspend fun getPendingExecutionsForShortcutForDeletion(shortcutId: ShortcutId): List<PendingExecutionModel>

    @Transaction
    open suspend fun deleteAll() {
        deleteAllPendingExecutions()
        deleteAllResolvedVariables()
    }

    @Query("DELETE FROM pending_execution")
    protected abstract suspend fun deleteAllPendingExecutions()

    @Query("DELETE FROM resolved_variable")
    protected abstract suspend fun deleteAllResolvedVariables()
}
