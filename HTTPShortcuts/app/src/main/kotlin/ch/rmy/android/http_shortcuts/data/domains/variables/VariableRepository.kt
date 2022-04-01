package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.detachFromRealm
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getBase
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.domains.getVariableById
import ch.rmy.android.http_shortcuts.data.domains.getVariableByKeyOrId
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class VariableRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getVariableByKeyOrId(keyOrId: VariableKeyOrId): Single<VariableModel> =
        queryItem {
            getVariableByKeyOrId(keyOrId)
        }

    fun getObservableVariables(): Observable<List<VariableModel>> =
        observeList {
            getBase().findFirst()!!.variables
        }

    fun getVariables(): Single<List<VariableModel>> =
        queryItem {
            getBase()
        }
            .map { base ->
                base.variables
            }

    fun setVariableValue(variableId: VariableId, value: String): Completable =
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.value = value
        }

    fun moveVariable(variableId1: VariableId, variableId2: VariableId) =
        commitTransaction {
            getBase()
                .findFirst()
                ?.variables
                ?.swap(variableId1, variableId2) { id }
        }

    fun duplicateVariable(variableId: VariableId, newKey: String) =
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

    fun deleteVariable(variableId: VariableId) =
        commitTransaction {
            getVariableById(variableId)
                .findFirst()
                ?.apply {
                    options?.deleteAllFromRealm()
                    deleteFromRealm()
                }
        }

    fun createTemporaryVariableFromVariable(variableId: VariableId): Completable =
        commitTransaction {
            val variable = getVariableById(variableId)
                .findFirst()!!
            copyVariable(variable, VariableModel.TEMPORARY_ID)
        }

    fun copyTemporaryVariableToVariable(variableId: VariableId) =
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

    private fun RealmTransactionContext.copyVariable(sourceVariable: VariableModel, targetVariableId: VariableId): VariableModel =
        sourceVariable.detachFromRealm()
            .apply {
                id = targetVariableId
                options?.forEach { option ->
                    option.id = newUUID()
                }
            }
            .let(::copyOrUpdate)
}
