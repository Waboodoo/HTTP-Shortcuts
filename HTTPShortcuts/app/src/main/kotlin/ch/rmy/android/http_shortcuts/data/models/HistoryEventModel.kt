package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_event")
data class HistoryEventModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "type")
    val type: String = "",
    @ColumnInfo(name = "data")
    val data: String = "",
    @ColumnInfo(name = "time")
    val time: Long = 0,
)
