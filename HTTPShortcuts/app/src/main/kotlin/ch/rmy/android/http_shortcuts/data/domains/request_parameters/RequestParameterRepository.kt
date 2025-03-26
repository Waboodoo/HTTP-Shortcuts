package ch.rmy.android.http_shortcuts.data.domains.request_parameters

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class RequestParameterRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getRequestParametersByShortcutId(shortcutId: ShortcutId): List<RequestParameter> = query {
        requestParameterDao().getRequestParametersByShortcutId(shortcutId)
    }

    suspend fun getRequestParametersByShortcutIds(shortcutIds: List<ShortcutId>): Map<ShortcutId, List<RequestParameter>> = query {
        if (shortcutIds.isEmpty()) {
            emptyMap()
        } else {
            requestParameterDao().getRequestParametersByShortcutIds(shortcutIds).groupBy { it.shortcutId }
        }
    }

    fun observeRequestParameters(shortcutId: ShortcutId): Flow<List<RequestParameter>> = queryFlow {
        requestParameterDao().observeRequestParametersByShortcutId(shortcutId)
    }

    suspend fun insertRequestParameter(
        key: String,
        value: String,
        parameterType: ParameterType,
        fileUploadType: FileUploadType?,
        fileUploadFileName: String?,
        fileUploadSourceFile: String?,
        fileUploadUseImageEditor: Boolean,
    ): RequestParameter =
        commitTransaction {
            val requestParameterDao = requestParameterDao()
            val requestParameter = RequestParameter(
                shortcutId = TEMPORARY_ID,
                key = key,
                value = value,
                parameterType = parameterType,
                fileUploadType = fileUploadType,
                fileUploadFileName = fileUploadFileName,
                fileUploadSourceFile = fileUploadSourceFile,
                fileUploadUseImageEditor = fileUploadUseImageEditor,
                sortingOrder = requestParameterDao.getRequestParameterCountByShortcutId(TEMPORARY_ID),
            )
            val newId = requestParameterDao.insertOrUpdateRequestParameter(requestParameter)
            requestParameter.copy(id = newId)
        }

    suspend fun updateRequestParameter(
        parameterId: RequestParameterId,
        key: String,
        value: String,
        fileUploadType: FileUploadType?,
        fileUploadFileName: String?,
        fileUploadSourceFile: String?,
        fileUploadUseImageEditor: Boolean,
    ) = commitTransaction {
        val requestParameterDao = requestParameterDao()
        val requestParameter = requestParameterDao.getRequestParameterById(parameterId).firstOrNull() ?: return@commitTransaction
        requestParameterDao.insertOrUpdateRequestParameter(
            requestParameter.copy(
                key = key,
                value = value,
                fileUploadType = fileUploadType,
                fileUploadFileName = fileUploadFileName,
                fileUploadSourceFile = fileUploadSourceFile,
                fileUploadUseImageEditor = fileUploadUseImageEditor,
            ),
        )
    }

    suspend fun moveRequestParameter(parameterId1: RequestParameterId, parameterId2: RequestParameterId) = commitTransaction {
        val requestParameterDao = requestParameterDao()
        val requestParameter1 = requestParameterDao.getRequestParameterById(parameterId1).firstOrNull() ?: return@commitTransaction
        val requestParameter2 = requestParameterDao.getRequestParameterById(parameterId2).firstOrNull() ?: return@commitTransaction
        assert(requestParameter1.shortcutId == requestParameter2.shortcutId)
        val shortcutId = requestParameter1.shortcutId
        if (requestParameter1.sortingOrder < requestParameter2.sortingOrder) {
            requestParameterDao.updateSortingOrder(
                shortcutId = shortcutId,
                from = requestParameter1.sortingOrder + 1,
                until = requestParameter2.sortingOrder,
                diff = -1,
            )
        } else {
            requestParameterDao.updateSortingOrder(
                shortcutId = shortcutId,
                from = requestParameter2.sortingOrder,
                until = requestParameter1.sortingOrder - 1,
                diff = 1,
            )
        }
        requestParameterDao.insertOrUpdateRequestParameter(requestParameter1.copy(sortingOrder = requestParameter2.sortingOrder))
    }

    suspend fun deleteRequestParameter(parameterId: RequestParameterId) = commitTransaction {
        val requestParameterDao = requestParameterDao()
        val requestParameter = requestParameterDao.getRequestParameterById(parameterId).firstOrNull()
            ?: return@commitTransaction
        requestParameterDao.deleteRequestParameter(parameterId)
        requestParameterDao.updateSortingOrder(
            shortcutId = requestParameter.shortcutId,
            from = requestParameter.sortingOrder,
            until = Int.MAX_VALUE,
            diff = -1,
        )
    }
}
