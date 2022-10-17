package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.OptionModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.realm.RealmList
import io.realm.kotlin.deleteFromRealm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemporaryVariableRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getObservableTemporaryVariable(): Flow<VariableModel> =
        observeItem {
            getTemporaryVariable()
        }

    suspend fun createNewTemporaryVariable(type: VariableType) {
        commitTransaction {
            copyOrUpdate(
                VariableModel(
                    id = VariableModel.TEMPORARY_ID,
                    variableType = type,
                )
            )
        }
    }

    suspend fun setKey(key: String) {
        commitTransactionForVariable { variable ->
            variable.key = key
        }
    }

    suspend fun setTitle(title: String) {
        commitTransactionForVariable { variable ->
            variable.title = title
        }
    }

    suspend fun setMessage(message: String) {
        commitTransactionForVariable { variable ->
            variable.message = message
        }
    }

    suspend fun setUrlEncode(enabled: Boolean) {
        commitTransactionForVariable { variable ->
            variable.urlEncode = enabled
        }
    }

    suspend fun setJsonEncode(enabled: Boolean) {
        commitTransactionForVariable { variable ->
            variable.jsonEncode = enabled
        }
    }

    suspend fun setSharingSupport(shareText: Boolean, shareTitle: Boolean) {
        commitTransactionForVariable { variable ->
            variable.isShareText = shareText
            variable.isShareTitle = shareTitle
        }
    }

    suspend fun setRememberValue(enabled: Boolean) {
        commitTransactionForVariable { variable ->
            variable.rememberValue = enabled
        }
    }

    suspend fun setMultiline(enabled: Boolean) {
        commitTransactionForVariable { variable ->
            variable.isMultiline = enabled
        }
    }

    suspend fun setValue(value: String) {
        commitTransactionForVariable { variable ->
            variable.value = value
        }
    }

    suspend fun setDataForType(data: Map<String, String?>) {
        commitTransactionForVariable { variable ->
            variable.dataForType = data
        }
    }

    private suspend fun commitTransactionForVariable(transaction: RealmTransactionContext.(VariableModel) -> Unit) {
        commitTransaction {
            transaction(
                getTemporaryVariable()
                    .findFirst()
                    ?: return@commitTransaction
            )
        }
    }

    suspend fun moveOption(optionId1: String, optionId2: String) {
        commitTransactionForVariable { variable ->
            variable.options?.swap(optionId1, optionId2) { id }
        }
    }

    suspend fun addOption(label: String, value: String) {
        commitTransactionForVariable { variable ->
            if (variable.options == null) {
                variable.options = RealmList()
            }
            variable.options!!.add(
                copy(
                    OptionModel(
                        label = label,
                        value = value,
                    )
                )
            )
        }
    }

    suspend fun updateOption(optionId: String, label: String, value: String) {
        commitTransactionForVariable { variable ->
            val option = variable.options
                ?.find { it.id == optionId }
                ?: return@commitTransactionForVariable
            option.label = label
            option.value = value
        }
    }

    suspend fun removeOption(optionId: String) {
        commitTransactionForVariable { variable ->
            variable.options
                ?.find { it.id == optionId }
                ?.deleteFromRealm()
        }
    }
}
