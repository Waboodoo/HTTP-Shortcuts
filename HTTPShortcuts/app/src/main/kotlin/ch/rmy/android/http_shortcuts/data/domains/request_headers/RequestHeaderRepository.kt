package ch.rmy.android.http_shortcuts.data.domains.request_headers

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class RequestHeaderRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getRequestHeadersByShortcutId(shortcutId: ShortcutId): List<RequestHeader> = query {
        requestHeaderDao().getRequestHeadersByShortcutId(shortcutId)
    }

    suspend fun getRequestHeadersByShortcutIds(shortcutIds: List<ShortcutId>): Map<ShortcutId, List<RequestHeader>> = query {
        if (shortcutIds.isEmpty()) {
            emptyMap()
        } else {
            requestHeaderDao().getRequestHeadersByShortcutIds(shortcutIds).groupBy { it.shortcutId }
        }
    }

    fun observeRequestHeaders(shortcutId: ShortcutId): Flow<List<RequestHeader>> = queryFlow {
        requestHeaderDao().observeRequestHeadersByShortcutId(shortcutId)
    }

    suspend fun insertRequestHeader(key: String, value: String): RequestHeader =
        commitTransaction {
            val requestHeaderDao = requestHeaderDao()
            val requestHeader = RequestHeader(
                shortcutId = TEMPORARY_ID,
                key = key,
                value = value,
                sortingOrder = requestHeaderDao.getRequestHeaderCountByShortcutId(TEMPORARY_ID),
            )
            val newId = requestHeaderDao.insertOrUpdateRequestHeader(requestHeader)
            requestHeader.copy(id = newId)
        }

    suspend fun updateRequestHeader(headerId: RequestHeaderId, key: String, value: String) = commitTransaction {
        val requestHeaderDao = requestHeaderDao()
        val requestHeader = requestHeaderDao.getRequestHeaderById(headerId).firstOrNull() ?: return@commitTransaction
        requestHeaderDao.insertOrUpdateRequestHeader(
            requestHeader.copy(
                key = key,
                value = value,
            ),
        )
    }

    suspend fun moveRequestHeader(headerId1: RequestHeaderId, headerId2: RequestHeaderId) = commitTransaction {
        val requestHeaderDao = requestHeaderDao()
        val requestHeader1 = requestHeaderDao.getRequestHeaderById(headerId1).firstOrNull() ?: return@commitTransaction
        val requestHeader2 = requestHeaderDao.getRequestHeaderById(headerId2).firstOrNull() ?: return@commitTransaction
        assert(requestHeader1.shortcutId == requestHeader2.shortcutId)
        val shortcutId = requestHeader1.shortcutId
        if (requestHeader1.sortingOrder < requestHeader2.sortingOrder) {
            requestHeaderDao.updateSortingOrder(
                shortcutId = shortcutId,
                from = requestHeader1.sortingOrder + 1,
                until = requestHeader2.sortingOrder,
                diff = -1,
            )
        } else {
            requestHeaderDao.updateSortingOrder(
                shortcutId = shortcutId,
                from = requestHeader2.sortingOrder,
                until = requestHeader1.sortingOrder - 1,
                diff = 1,
            )
        }
        requestHeaderDao.insertOrUpdateRequestHeader(requestHeader1.copy(sortingOrder = requestHeader2.sortingOrder))
    }

    suspend fun deleteRequestHeader(headerId: RequestHeaderId) = commitTransaction {
        val requestHeaderDao = requestHeaderDao()
        val requestHeader = requestHeaderDao.getRequestHeaderById(headerId).firstOrNull()
            ?: return@commitTransaction
        requestHeaderDao.deleteRequestHeader(headerId)
        requestHeaderDao.updateSortingOrder(
            shortcutId = requestHeader.shortcutId,
            from = requestHeader.sortingOrder,
            until = Int.MAX_VALUE,
            diff = -1,
        )
    }
}
