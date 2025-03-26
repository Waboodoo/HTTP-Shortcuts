package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.enums.HistoryEventType
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import java.time.Instant

@Entity(tableName = "history_event")
data class HistoryEvent(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "type")
    val type: HistoryEventType,
    @ColumnInfo(name = "data")
    val data: String,
    @ColumnInfo(name = "time", index = true)
    val time: Instant,
) {
    fun <T> getEventData(dataClass: Class<T>): T =
        GsonUtil.gson.fromJson(data, dataClass)

    companion object {
        inline fun <reified T> HistoryEvent.getEventData(): T =
            getEventData(T::class.java)
    }
}
