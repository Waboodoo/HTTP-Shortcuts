package ch.rmy.android.http_shortcuts.data.domains.variables

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GlobalVariableDao {
    @Query("SELECT * FROM variable WHERE id != ${GlobalVariable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract suspend fun getVariables(): List<GlobalVariable>

    @Query("SELECT * FROM variable WHERE id != ${GlobalVariable.TEMPORARY_ID} ORDER BY sorting_order ASC")
    abstract fun observeVariables(): Flow<List<GlobalVariable>>

    @Query("SELECT * FROM variable WHERE id == ${GlobalVariable.TEMPORARY_ID}")
    abstract fun observeTemporaryVariable(): Flow<GlobalVariable?>

    @Query("SELECT * FROM variable WHERE id = :id LIMIT 1")
    abstract suspend fun getVariableById(id: GlobalVariableId): List<GlobalVariable>

    @Query("SELECT * FROM variable WHERE `key` = :keyOrId OR id = :keyOrId")
    abstract suspend fun getVariableByKeyOrId(keyOrId: String): List<GlobalVariable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrUpdateVariable(variable: GlobalVariable)

    @Query("DELETE FROM variable WHERE id != ${GlobalVariable.TEMPORARY_ID}")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun update(id: GlobalVariableId, transformation: (GlobalVariable) -> GlobalVariable) {
        getVariableById(id)
            .firstOrNull()
            ?.let(transformation)
            ?.let { insertOrUpdateVariable(it) }
    }

    @Query("DELETE FROM variable WHERE id = :id")
    protected abstract suspend fun deleteById(id: GlobalVariableId)

    @Transaction
    open suspend fun duplicate(globalVariableId: GlobalVariableId, newKey: String) {
        val variable = getVariableById(globalVariableId).firstOrNull() ?: return
        updateSortingOrder(from = variable.sortingOrder + 1, until = Int.MAX_VALUE, diff = 1)
        insertOrUpdateVariable(
            variable.copy(
                id = UUIDUtils.newUUID(),
                key = newKey,
                sortingOrder = variable.sortingOrder + 1,
            ),
        )
    }

    // TODO(???): Consider moving all the @Transaction-annotated logic into the VariableRepository
    @Transaction
    open suspend fun swap(globalVariableId1: GlobalVariableId, globalVariableId2: GlobalVariableId) {
        val variable1 = getVariableById(globalVariableId1).firstOrNull() ?: return
        val variable2 = getVariableById(globalVariableId2).firstOrNull() ?: return
        if (variable1.sortingOrder < variable2.sortingOrder) {
            updateSortingOrder(from = variable1.sortingOrder + 1, until = variable2.sortingOrder, diff = -1)
        } else {
            updateSortingOrder(from = variable2.sortingOrder, until = variable1.sortingOrder - 1, diff = 1)
        }
        insertOrUpdateVariable(variable1.copy(sortingOrder = variable2.sortingOrder))
    }

    @Transaction
    open suspend fun delete(globalVariableId: GlobalVariableId) {
        val variable = getVariableById(globalVariableId).firstOrNull() ?: return
        deleteById(globalVariableId)
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
    open suspend fun saveTemporaryVariable(globalVariableId: GlobalVariableId) {
        val existingVariable = getVariableById(globalVariableId).firstOrNull()
        val temporaryVariable = getVariableById(GlobalVariable.TEMPORARY_ID).firstOrNull() ?: return

        insertOrUpdateVariable(
            temporaryVariable.copy(
                id = globalVariableId,
                sortingOrder = existingVariable?.sortingOrder ?: (getMaxSortingOrder() + 1),
            ),
        )
    }

    @Query("SELECT MAX(sorting_order) AS max_sorting_order FROM variable")
    protected abstract suspend fun getMaxSortingOrder(): Int

    @Query("SELECT value FROM variable WHERE secret = 1")
    abstract suspend fun getSecretValues(): List<String>
}
