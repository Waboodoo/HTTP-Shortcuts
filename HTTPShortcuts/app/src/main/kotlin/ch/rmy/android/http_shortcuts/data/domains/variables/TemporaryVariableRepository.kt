package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.reactivex.Completable
import io.reactivex.Observable
import io.realm.RealmList

class TemporaryVariableRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getObservableTemporaryVariable(): Observable<Variable> =
        observeItem {
            getTemporaryVariable()
        }

    fun createNewTemporaryVariable(type: VariableType): Completable =
        commitTransaction {
            copyOrUpdate(
                Variable(
                    id = Variable.TEMPORARY_ID,
                    variableType = type,
                )
            )
        }

    fun setKey(key: String): Completable =
        commitTransactionForVariable { variable ->
            variable.key = key
        }

    fun setTitle(title: String): Completable =
        commitTransactionForVariable { variable ->
            variable.title = title
        }

    fun setUrlEncode(enabled: Boolean): Completable =
        commitTransactionForVariable { variable ->
            variable.urlEncode = enabled
        }

    fun setJsonEncode(enabled: Boolean): Completable =
        commitTransactionForVariable { variable ->
            variable.jsonEncode = enabled
        }

    fun setShareText(enabled: Boolean): Completable =
        commitTransactionForVariable { variable ->
            variable.isShareText = enabled
        }

    fun setRememberValue(enabled: Boolean): Completable =
        commitTransactionForVariable { variable ->
            variable.rememberValue = enabled
        }

    fun setMultiline(enabled: Boolean): Completable =
        commitTransactionForVariable { variable ->
            variable.isMultiline = enabled
        }

    fun setValue(value: String): Completable =
        commitTransactionForVariable { variable ->
            variable.value = value
        }

    fun setDataForType(data: Map<String, String?>): Completable =
        commitTransactionForVariable { variable ->
            variable.dataForType = data
        }

    private fun commitTransactionForVariable(transaction: RealmTransactionContext.(Variable) -> Unit) =
        commitTransaction {
            transaction(
                getTemporaryVariable()
                    .findFirst()
                    ?: return@commitTransaction
            )
        }

    fun moveOption(optionId1: String, optionId2: String) =
        commitTransactionForVariable { variable ->
            variable.options?.swap(optionId1, optionId2) { id }
        }

    fun addOption(label: String, value: String) =
        commitTransactionForVariable { variable ->
            if (variable.options == null) {
                variable.options = RealmList()
            }
            variable.options!!.add(
                copy(
                    Option(
                        label = label,
                        value = value,
                    )
                )
            )
        }

    fun updateOption(optionId: String, label: String, value: String) =
        commitTransactionForVariable { variable ->
            val option = variable.options
                ?.find { it.id == optionId }
                ?: return@commitTransactionForVariable
            option.label = label
            option.value = value
        }

    fun removeOption(optionId: String) =
        commitTransactionForVariable { variable ->
            variable.options
                ?.find { it.id == optionId }
                ?.deleteFromRealm()
        }
}
