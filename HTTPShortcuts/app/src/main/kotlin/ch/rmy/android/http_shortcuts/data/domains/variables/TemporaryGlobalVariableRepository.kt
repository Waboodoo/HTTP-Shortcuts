package ch.rmy.android.http_shortcuts.data.domains.variables

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import org.json.JSONObject

class TemporaryGlobalVariableRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    fun observeTemporaryVariable(): Flow<GlobalVariable> = queryFlow {
        globalVariableDao()
            .observeTemporaryVariable()
            .filterNotNull()
            .distinctUntilChanged()
    }

    suspend fun createNewTemporaryVariable(type: VariableType) = query {
        globalVariableDao()
            .insertOrUpdateVariable(
                GlobalVariable(
                    id = GlobalVariable.TEMPORARY_ID,
                    type = type,
                    key = "",
                    value = "",
                    data = null,
                    rememberValue = false,
                    urlEncode = false,
                    jsonEncode = false,
                    title = "",
                    message = "",
                    isShareText = false,
                    isShareTitle = false,
                    isMultiline = false,
                    isExcludeValueFromExport = false,
                ),
            )
    }

    suspend fun setKey(key: String) {
        update {
            it.copy(key = key)
        }
    }

    suspend fun setTitle(title: String) {
        update {
            it.copy(title = title)
        }
    }

    suspend fun setMessage(message: String) {
        update {
            it.copy(message = message)
        }
    }

    suspend fun setUrlEncode(enabled: Boolean) {
        update {
            it.copy(urlEncode = enabled)
        }
    }

    suspend fun setJsonEncode(enabled: Boolean) {
        update {
            it.copy(jsonEncode = enabled)
        }
    }

    suspend fun setSharingSupport(shareText: Boolean, shareTitle: Boolean) {
        update {
            it.copy(
                isShareText = shareText,
                isShareTitle = shareTitle,
            )
        }
    }

    suspend fun setExcludeValueFromExports(exclude: Boolean) {
        update {
            it.copy(isExcludeValueFromExport = exclude)
        }
    }

    suspend fun setRememberValue(enabled: Boolean) {
        update {
            it.copy(rememberValue = enabled)
        }
    }

    suspend fun setMultiline(enabled: Boolean) {
        update {
            it.copy(isMultiline = enabled)
        }
    }

    suspend fun setValue(value: String) {
        update {
            it.copy(value = value)
        }
    }

    suspend fun setData(value: Map<String, Any?>) {
        update {
            it.copy(data = JSONObject(value).toString())
        }
    }

    private suspend fun update(transform: (GlobalVariable) -> GlobalVariable) = query {
        globalVariableDao().update(GlobalVariable.TEMPORARY_ID, transform)
    }
}
