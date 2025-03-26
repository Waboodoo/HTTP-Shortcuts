package ch.rmy.android.http_shortcuts.data.domains.history

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class HistoryRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    fun observeHistory(maxAge: Duration): Flow<List<HistoryEvent>> = queryFlow {
        historyEventDao()
            .observeNewerThan(Instant.now().toEpochMilli() - maxAge.inWholeMilliseconds)
            .distinctUntilChanged()
    }

    suspend fun deleteHistory() = query {
        historyEventDao().deleteAll()
    }

    suspend fun deleteOldEvents(maxAge: Duration) = query {
        historyEventDao().deleteOlderThan(Instant.now().toEpochMilli() - maxAge.inWholeMilliseconds)
    }

    suspend fun storeHistoryEvent(type: HistoryEventType, data: Any?) = query {
        historyEventDao().insert(
            HistoryEvent(
                type = type,
                data = withContext(Dispatchers.Default) {
                    GsonUtil.toJson(data)
                },
                time = Instant.now(),
            ),
        )
    }
}
