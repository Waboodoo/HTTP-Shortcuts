package ch.rmy.android.http_shortcuts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VariableDao {
    @Query("SELECT * FROM variable WHERE id != ${Variable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract suspend fun get(): List<Variable>

    @Query("SELECT * FROM variable WHERE id != ${Variable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract fun observe(): Flow<List<Variable>>

    @Query("SELECT * FROM variable WHERE id == ${Variable.TEMPORARY_ID}")
    abstract fun observeTemporaryVariable(): Flow<Variable?>

    @Query("SELECT * FROM variable WHERE id = :id")
    abstract suspend fun getById(id: VariableId): List<Variable>

    @Query("SELECT * FROM variable WHERE `key` = :keyOrId OR id = :keyOrId")
    abstract suspend fun getByKeyOrId(keyOrId: VariableKeyOrId): List<Variable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(variable: Variable)

    @Transaction
    open suspend fun replaceAll(variables: List<Variable>) {
        deleteAll()
        variables.forEachIndexed { index, variable ->
            insert(
                variable.copy(
                    sortingOrder = index,
                ),
            )
        }
    }

    @Transaction
    open suspend fun mergeAll(variables: List<Variable>) {
        val existingVariables = get()
        val newVariables = variables.associateBy { it.id }

        existingVariables.forEach { variable ->
            newVariables[variable.id]?.let { newVariable ->
                insert(
                    newVariable.copy(
                        sortingOrder = variable.sortingOrder,
                    ),
                )
            }
        }

        var sortingOrder = existingVariables.size
        val existingVariableIds = existingVariables.map { it.id }.toSet()
        variables
            .filter { it.id !in existingVariableIds }
            .forEach { variable ->
                insert(
                    variable.copy(
                        sortingOrder = sortingOrder,
                    ),
                )
                sortingOrder++
            }
    }

    @Query("DELETE FROM variable WHERE id != ${Variable.TEMPORARY_ID}")
    protected abstract suspend fun deleteAll()

    @Transaction
    open suspend fun update(id: VariableId, transformation: (Variable) -> Variable) {
        getById(id)
            .firstOrNull()
            ?.let(transformation)
            ?.let { insert(it) }
    }

    @Query("DELETE FROM variable WHERE id = :id")
    protected abstract suspend fun deleteById(id: VariableId)

    @Transaction
    open suspend fun duplicate(variableId: VariableId, newKey: String) {
        val variable = getById(variableId).firstOrNull() ?: return
        updateSortingOrder(from = variable.sortingOrder + 1, until = Int.MAX_VALUE, diff = 1)
        insert(
            variable.copy(
                id = newUUID(),
                key = newKey,
                sortingOrder = variable.sortingOrder + 1,
            ),
        )
    }

    @Transaction
    open suspend fun swap(variableId1: VariableId, variableId2: VariableId) {
        val variable1 = getById(variableId1).firstOrNull() ?: return
        val variable2 = getById(variableId2).firstOrNull() ?: return
        if (variable1.sortingOrder < variable2.sortingOrder) {
            updateSortingOrder(from = variable1.sortingOrder + 1, until = variable2.sortingOrder, diff = -1)
        } else {
            updateSortingOrder(from = variable2.sortingOrder, until = variable1.sortingOrder - 1, diff = 1)
        }
        insert(variable1.copy(sortingOrder = variable2.sortingOrder))
    }

    @Transaction
    open suspend fun delete(variableId: VariableId) {
        val variable = getById(variableId).firstOrNull() ?: return
        deleteById(variableId)
        updateSortingOrder(from = variable.sortingOrder, until = Int.MAX_VALUE, diff = -1)
    }

    @Query("UPDATE variable SET sorting_order = sorting_order + :diff WHERE sorting_order >= :from AND sorting_order <= :until")
    protected abstract suspend fun updateSortingOrder(from: Int, until: Int, diff: Int)

    @Transaction
    open suspend fun sortAlphabetically() {
        get()
            .sortedBy { it.key.lowercase() }
            .forEachIndexed { index, variable ->
                if (index != variable.sortingOrder) {
                    insert(
                        variable.copy(
                            sortingOrder = index,
                        ),
                    )
                }
            }
    }

    @Transaction
    open suspend fun saveTemporaryVariable(variableId: VariableId?) {
        val existingVariable = variableId?.let { getById(it) }?.firstOrNull()
        val temporaryVariable = getById(Variable.TEMPORARY_ID).firstOrNull() ?: return

        insert(
            temporaryVariable.copy(
                id = existingVariable?.id ?: newUUID(),
                sortingOrder = existingVariable?.sortingOrder ?: (getMaxSortingOrder() + 1),
            ),
        )
    }

    @Query("SELECT MAX(sorting_order) AS max_sorting_order FROM variable")
    protected abstract suspend fun getMaxSortingOrder(): Int

    @Transaction
    @Deprecated("Must only be used for Realm-to-Room migration")
    open suspend fun insertAll(variables: List<Variable>) {
        variables.forEach { variable ->
            insert(variable)
        }
    }
}
