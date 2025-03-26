package ch.rmy.android.http_shortcuts.data.domains.variables

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VariableDao {
    @Query("SELECT * FROM variable WHERE id != ${Variable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract suspend fun getVariables(): List<Variable>

    @Query("SELECT * FROM variable WHERE id != ${Variable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract fun observeVariables(): Flow<List<Variable>>

    @Query("SELECT * FROM variable WHERE id == ${Variable.TEMPORARY_ID}")
    abstract fun observeTemporaryVariable(): Flow<Variable?>

    @Query("SELECT * FROM variable WHERE id = :id LIMIT 1")
    abstract suspend fun getVariableById(id: VariableId): List<Variable>

    @Query("SELECT * FROM variable WHERE `key` = :keyOrId OR id = :keyOrId")
    abstract suspend fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): List<Variable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateVariable(variable: Variable)

    @Query("DELETE FROM variable WHERE id != ${Variable.TEMPORARY_ID}")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun update(id: VariableId, transformation: (Variable) -> Variable) {
        getVariableById(id)
            .firstOrNull()
            ?.let(transformation)
            ?.let { insertOrUpdateVariable(it) }
    }

    @Query("DELETE FROM variable WHERE id = :id")
    protected abstract suspend fun deleteById(id: VariableId)

    @Transaction
    open suspend fun duplicate(variableId: VariableId, newKey: String) {
        val variable = getVariableById(variableId).firstOrNull() ?: return
        updateSortingOrder(from = variable.sortingOrder + 1, until = Int.MAX_VALUE, diff = 1)
        insertOrUpdateVariable(
            variable.copy(
                id = UUIDUtils.newUUID(),
                key = newKey,
                sortingOrder = variable.sortingOrder + 1,
            ),
        )
    }

    // TODO(room): Consider moving all the @Transaction-annotated logic into the VariableRepository
    @Transaction
    open suspend fun swap(variableId1: VariableId, variableId2: VariableId) {
        val variable1 = getVariableById(variableId1).firstOrNull() ?: return
        val variable2 = getVariableById(variableId2).firstOrNull() ?: return
        if (variable1.sortingOrder < variable2.sortingOrder) {
            updateSortingOrder(from = variable1.sortingOrder + 1, until = variable2.sortingOrder, diff = -1)
        } else {
            updateSortingOrder(from = variable2.sortingOrder, until = variable1.sortingOrder - 1, diff = 1)
        }
        insertOrUpdateVariable(variable1.copy(sortingOrder = variable2.sortingOrder))
    }

    @Transaction
    open suspend fun delete(variableId: VariableId) {
        val variable = getVariableById(variableId).firstOrNull() ?: return
        deleteById(variableId)
        updateSortingOrder(from = variable.sortingOrder, until = Int.MAX_VALUE, diff = -1)
    }

    @Query("UPDATE variable SET sorting_order = sorting_order + :diff WHERE sorting_order >= :from AND sorting_order <= :until")
    protected abstract suspend fun updateSortingOrder(from: Int, until: Int, diff: Int)

    @Transaction
    open suspend fun sortAlphabetically() {
        getVariables()
            .sortedBy { it.key.lowercase() }
            .forEachIndexed { index, variable ->
                if (index != variable.sortingOrder) {
                    insertOrUpdateVariable(
                        variable.copy(
                            sortingOrder = index,
                        ),
                    )
                }
            }
    }

    @Transaction
    open suspend fun saveTemporaryVariable(variableId: VariableId?) {
        val existingVariable = variableId?.let { getVariableById(it) }?.firstOrNull()
        val temporaryVariable = getVariableById(Variable.TEMPORARY_ID).firstOrNull() ?: return

        insertOrUpdateVariable(
            temporaryVariable.copy(
                id = existingVariable?.id ?: UUIDUtils.newUUID(),
                sortingOrder = existingVariable?.sortingOrder ?: (getMaxSortingOrder() + 1),
            ),
        )
    }

    @Query("SELECT MAX(sorting_order) AS max_sorting_order FROM variable")
    protected abstract suspend fun getMaxSortingOrder(): Int
}
