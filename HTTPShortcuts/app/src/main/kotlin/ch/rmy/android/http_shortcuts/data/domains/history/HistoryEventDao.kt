package ch.rmy.android.http_shortcuts.data.domains.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryEventDao {
    @Query("SELECT * FROM history_event WHERE time > :threshold ORDER BY time DESC")
    fun observeNewerThan(threshold: Long): Flow<List<HistoryEvent>>

    @Insert
    suspend fun insert(historyEvent: HistoryEvent)

    @Query("DELETE FROM history_event WHERE time < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("DELETE FROM history_event")
    suspend fun deleteAll()
}
