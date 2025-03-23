package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.import_export.Importer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class VariableRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    suspend fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): Variable =
        get(Database::variableDao)
            .getByKeyOrId(keyOrId)
            .first()

    fun observeVariables(): Flow<List<Variable>> =
        flow(Database::variableDao) {
            observe()
                .distinctUntilChanged()
        }

    suspend fun getVariables(): List<Variable> =
        get(Database::variableDao).get()

    suspend fun setVariableValue(variableId: VariableId, value: String) {
        get(Database::variableDao)
            .update(variableId) {
                it.copy(
                    value = value,
                )
            }
    }

    suspend fun moveVariable(variableId1: VariableId, variableId2: VariableId) {
        get(Database::variableDao).swap(variableId1, variableId2)
    }

    suspend fun duplicateVariable(variableId: VariableId, newKey: String) {
        get(Database::variableDao)
            .duplicate(variableId, newKey)
    }

    suspend fun deleteVariable(variableId: VariableId) {
        get(Database::variableDao)
            .delete(variableId)
    }

    suspend fun createTemporaryVariableFromVariable(variableId: VariableId) {
        get(Database::variableDao)
            .update(variableId) {
                it.copy(id = Variable.TEMPORARY_ID)
            }
    }

    suspend fun copyTemporaryVariableToVariable(variableId: VariableId) {
        get(Database::variableDao)
            .saveTemporaryVariable(variableId)
    }

    suspend fun sortVariablesAlphabetically() {
        get(Database::variableDao)
            .sortAlphabetically()
    }

    suspend fun import(variables: List<Variable>, mode: Importer.ImportMode) {
        with(get(Database::variableDao)) {
            when (mode) {
                Importer.ImportMode.MERGE -> mergeAll(variables)
                Importer.ImportMode.REPLACE -> replaceAll(variables)
            }
        }
    }
}
