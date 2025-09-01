package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Entity(tableName = "sync_config")
data class SyncConfig(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sync_type")
    val type: SyncType,
    @ColumnInfo(name = "target")
    val target: String,
    @ColumnInfo(name = "schedule")
    val schedule: SyncSchedule,
    @ColumnInfo(name = "password")
    val password: String = "",
)
