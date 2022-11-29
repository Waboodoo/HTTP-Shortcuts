package ch.rmy.android.http_shortcuts.data.domains.history

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.getHistoryEvents
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel.Companion.FIELD_TIME
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import kotlin.time.Duration

class HistoryRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getObservableHistory(maxAge: Duration): Flow<List<HistoryEventModel>> =
        observeQuery {
            getHistoryEvents()
                .greaterThan(FIELD_TIME, Date().apply { time -= maxAge.inWholeMilliseconds })
        }

    suspend fun deleteHistory() {
        commitTransaction {
            getHistoryEvents()
                .findAll()
                .deleteAllFromRealm()
        }
    }

    suspend fun deleteOldEvents(maxAge: Duration) {
        commitTransaction {
            getHistoryEvents()
                .lessThan(FIELD_TIME, Date().apply { time -= maxAge.inWholeMilliseconds })
                .findAll()
                .deleteAllFromRealm()
        }
    }

    suspend fun storeHistoryEvent(type: HistoryEventType, data: Any?) {
        commitTransaction {
            copy(
                HistoryEventModel(id = newUUID(), eventType = type, eventData = data)
            )
        }
    }
}
