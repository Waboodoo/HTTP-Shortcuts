package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GlobalVariableRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): GlobalVariable = query {
        globalVariableDao()
            .getVariableByKeyOrId(keyOrId)
            .first()
    }

    fun observeVariables(): Flow<List<GlobalVariable>> = queryFlow {
        globalVariableDao().observeVariables()
    }

    suspend fun getGlobalVariables(): List<GlobalVariable> = query {
        globalVariableDao().getVariables()
    }

    suspend fun setVariableValue(globalVariableId: GlobalVariableId, value: String) = query {
        globalVariableDao()
            .update(globalVariableId) {
                it.copy(
                    value = value,
                )
            }
    }

    suspend fun moveVariable(globalVariableId1: GlobalVariableId, globalVariableId2: GlobalVariableId) = query {
        globalVariableDao().swap(globalVariableId1, globalVariableId2)
    }

    suspend fun duplicateVariable(globalVariableId: GlobalVariableId, newKey: String) = query {
        globalVariableDao().duplicate(globalVariableId, newKey)
    }

    suspend fun deleteVariable(globalVariableId: GlobalVariableId) = query {
        globalVariableDao().delete(globalVariableId)
    }

    suspend fun createTemporaryVariableFromVariable(globalVariableId: GlobalVariableId) = query {
        globalVariableDao()
            .update(globalVariableId) {
                it.copy(id = GlobalVariable.TEMPORARY_ID)
            }
    }

    suspend fun copyTemporaryVariableToVariable(globalVariableId: GlobalVariableId) = query {
        globalVariableDao().saveTemporaryVariable(globalVariableId)
    }

    suspend fun sortVariablesAlphabetically() = query {
        globalVariableDao().sortAlphabetically()
    }
}
