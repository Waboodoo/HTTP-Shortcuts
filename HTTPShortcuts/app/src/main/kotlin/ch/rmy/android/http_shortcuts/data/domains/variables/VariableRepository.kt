package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.detachFromRealm
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.domains.getVariableById
import ch.rmy.android.http_shortcuts.data.domains.getVariableByKeyOrId
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VariableRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getVariableByKeyOrId(keyOrId: String): Single<Variable> =
        queryItem {
            getVariableByKeyOrId(keyOrId)
        }

    fun getObservableVariables(): Observable<List<Variable>> =
        observeList {
            getBase().findFirst()!!.variables
        }

    fun getVariables(): Single<List<Variable>> =
        queryItem {
            getBase()
        }
            .map { base ->
                base.variables
            }

    fun setVariableValue(variableId: String, value: String): Completable =
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.value = value
        }

    fun moveVariable(variableId: String, position: Int) =
        commitTransaction {
            val variable = getVariableById(variableId)
                .findFirst()
                ?: return@commitTransaction
            val variables = getBase()
                .findFirst()
                ?.variables ?: return@commitTransaction
            val oldPosition = variables.indexOf(variable)
            variables.move(oldPosition, position)
        }

    fun duplicateVariable(variableId: String, newKey: String) =
        commitTransaction {
            val oldVariable = getVariableById(variableId)
                .findFirst()
                ?: return@commitTransaction
            val newVariable = oldVariable.detachFromRealm()
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

    fun deleteVariable(variableId: String) =
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.apply {
                    options?.deleteAllFromRealm()
                    deleteFromRealm()
                }
        }

    fun createTemporaryVariableFromVariable(variableId: String): Completable =
        commitTransaction {
            val variable = getVariableById(variableId)
                .findFirst()!!
            copyVariable(variable, Variable.TEMPORARY_ID)
        }

    fun copyTemporaryVariableToVariable(variableId: String) =
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

    private fun RealmTransactionContext.copyVariable(sourceVariable: Variable, targetVariableId: String): Variable =
        sourceVariable.detachFromRealm()
            .apply {
                id = targetVariableId
                options?.forEach { option ->
                    option.id = newUUID()
                }
            }
            .let(::copyOrUpdate)
}
