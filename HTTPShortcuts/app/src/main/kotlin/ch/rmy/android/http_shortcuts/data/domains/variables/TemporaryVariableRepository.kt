package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryVariable
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TemporaryVariableRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getObservableTemporaryVariable(): Flow<Variable> =
        observeItem {
            getTemporaryVariable()
        }

    suspend fun createNewTemporaryVariable(type: VariableType) {
        commitTransaction {
            copyOrUpdate(
                Variable(
                    id = Variable.TEMPORARY_ID,
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

    suspend fun setExcludeValueFromExports(exclude: Boolean) {
        commitTransactionForVariable { variable ->
            variable.isExcludeValueFromExport = exclude
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

    private suspend fun commitTransactionForVariable(transaction: RealmTransactionContext.(Variable) -> Unit) {
        commitTransaction {
            transaction(
                getTemporaryVariable()
                    .findFirst()
                    ?: return@commitTransaction
            )
        }
    }

    suspend fun setOptions(options: List<Option>) {
        commitTransactionForVariable { variable ->
            variable.options?.deleteAll()
            variable.options = realmListOf<Option>()
                .apply {
                    options.forEach {
                        add(copy(it))
                    }
                }
        }
    }
}
