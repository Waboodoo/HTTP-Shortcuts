package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.domains.getVariableById
import ch.rmy.android.http_shortcuts.data.domains.getVariableByKeyOrId
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.realm.kotlin.ext.copyFromRealm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VariableRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    suspend fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): Variable =
        queryItem {
            getVariableByKeyOrId(keyOrId)
        }

    fun getObservableVariables(): Flow<List<Variable>> =
        observeList {
            getBase().findFirst()!!.variables
        }

    suspend fun getVariables(): List<Variable> =
        queryItem {
            getBase()
        }
            .variables

    suspend fun setVariableValue(variableId: VariableId, value: String) {
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.value = value
        }
    }

    suspend fun moveVariable(variableId1: VariableId, variableId2: VariableId) {
        commitTransaction {
            getBase()
                .findFirst()
                ?.variables
                ?.swap(variableId1, variableId2) { id }
        }
    }

    suspend fun duplicateVariable(variableId: VariableId, newKey: String) {
        commitTransaction {
            val oldVariable = getVariableById(variableId)
                .findFirst()
                ?: return@commitTransaction
            val newVariable = oldVariable.copyFromRealm()
            newVariable.id = newUUID()
            newVariable.key = newKey
            newVariable.options?.forEach {
                it.id = newUUID()
            }

            val base = getBase()
                .findFirst()
                ?: return@commitTransaction
            val oldPosition = base.variables.indexOf(oldVariable)
            val newPersistedVariable = copyOrUpdate(newVariable)
            base.variables.add(oldPosition + 1, newPersistedVariable)
        }
    }

    suspend fun deleteVariable(variableId: VariableId) {
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.apply {
                    options?.deleteAll()
                    delete()
                }
        }
    }

    suspend fun createTemporaryVariableFromVariable(variableId: VariableId) {
        commitTransaction {
            val variable = getVariableById(variableId)
                .findFirst()!!
            copyVariable(variable, Variable.TEMPORARY_ID)
        }
    }

    suspend fun copyTemporaryVariableToVariable(variableId: VariableId) {
        commitTransaction {
            val temporaryVariable = getTemporaryVariable()
                .findFirst() ?: return@commitTransaction
            val variable = copyVariable(temporaryVariable, variableId)
            val base = getBase()
                .findFirst() ?: return@commitTransaction
            if (base.variables.none { it.id == variableId }) {
                base.variables.add(variable)
            }
        }
    }

    private fun RealmTransactionContext.copyVariable(sourceVariable: Variable, targetVariableId: VariableId): Variable =
        sourceVariable.copyFromRealm()
            .apply {
                id = targetVariableId
                options?.forEach { option ->
                    option.id = newUUID()
                }
            }
            .let(::copyOrUpdate)

    suspend fun sortVariablesAlphabetically() {
        commitTransaction {
            val base = getBase()
                .findFirst()
                ?: return@commitTransaction

            val sortedVariables = base.variables.sortedBy { it.key.lowercase() }
            base.variables.clear()
            base.variables.addAll(sortedVariables)
        }
    }
}
