package ch.rmy.android.http_shortcuts.data.domains.history

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.getHistoryEvents
import ch.rmy.android.http_shortcuts.data.domains.getHistoryEventsNewerThan
import ch.rmy.android.http_shortcuts.data.domains.getHistoryEventsOlderThan
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration

class HistoryRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getObservableHistory(maxAge: Duration): Flow<List<HistoryEvent>> =
        observeQuery {
            getHistoryEventsNewerThan(maxAge)
        }

    suspend fun deleteHistory() {
        commitTransaction {
            getHistoryEvents().deleteAll()
        }
    }

    suspend fun deleteOldEvents(maxAge: Duration) {
        commitTransaction {
            getHistoryEventsOlderThan(maxAge).deleteAll()
        }
    }

    suspend fun storeHistoryEvent(type: HistoryEventType, data: Any?) {
        commitTransaction {
            copy(
                HistoryEvent(id = newUUID(), eventType = type, eventData = data)
            )
        }
    }
}
