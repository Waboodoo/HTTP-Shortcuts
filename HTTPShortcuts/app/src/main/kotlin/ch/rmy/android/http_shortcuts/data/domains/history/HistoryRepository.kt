package ch.rmy.android.http_shortcuts.data.domains.history

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class HistoryRepository
@Inject
constructor(
    database: Database,
) {
    private val eventHistoryDao = database.historyEventDao()

    fun getObservableHistory(maxAge: Duration): Flow<List<HistoryEvent>> =
        eventHistoryDao.observeNewerThan(Instant.now().toEpochMilli() - maxAge.inWholeMilliseconds)
            .distinctUntilChanged()
            .map { events ->
                events.map { event ->
                    HistoryEvent(
                        id = event.id,
                        type = HistoryEventType.parse(event.type),
                        time = Instant.ofEpochMilli(event.time),
                        data = event.data,
                    )
                }
            }

    suspend fun deleteHistory() {
        eventHistoryDao.deleteAll()
    }

    suspend fun deleteOldEvents(maxAge: Duration) {
        eventHistoryDao.deleteOlderThan(Instant.now().toEpochMilli() - maxAge.inWholeMilliseconds)
    }

    suspend fun storeHistoryEvent(type: HistoryEventType, data: Any?) {
        eventHistoryDao.insert(
            HistoryEventModel(
                type = type.type,
                data = withContext(Dispatchers.Default) {
                    GsonUtil.toJson(data)
                },
                time = Instant.now().toEpochMilli(),
            ),
        )
    }
}
