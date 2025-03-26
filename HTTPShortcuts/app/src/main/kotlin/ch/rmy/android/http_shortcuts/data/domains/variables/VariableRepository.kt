package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class VariableRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): Variable = query {
        variableDao()
            .getVariableByKeyOrId(keyOrId)
            .first()
    }

    fun observeVariables(): Flow<List<Variable>> = queryFlow {
        variableDao().observeVariables()
    }

    suspend fun getVariables(): List<Variable> = query {
        variableDao().getVariables()
    }

    suspend fun setVariableValue(variableId: VariableId, value: String) = query {
        variableDao()
            .update(variableId) {
                it.copy(
                    value = value,
                )
            }
    }

    suspend fun moveVariable(variableId1: VariableId, variableId2: VariableId) = query {
        variableDao().swap(variableId1, variableId2)
    }

    suspend fun duplicateVariable(variableId: VariableId, newKey: String) = query {
        variableDao().duplicate(variableId, newKey)
    }

    suspend fun deleteVariable(variableId: VariableId) = query {
        variableDao().delete(variableId)
    }

    suspend fun createTemporaryVariableFromVariable(variableId: VariableId) = query {
        variableDao()
            .update(variableId) {
                it.copy(id = Variable.TEMPORARY_ID)
            }
    }

    suspend fun copyTemporaryVariableToVariable(variableId: VariableId) = query {
        variableDao().saveTemporaryVariable(variableId)
    }

    suspend fun sortVariablesAlphabetically() = query {
        variableDao().sortAlphabetically()
    }
}
