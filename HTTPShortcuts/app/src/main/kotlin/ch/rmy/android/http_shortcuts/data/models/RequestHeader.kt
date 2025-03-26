package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Entity(tableName = "request_header")
data class RequestHeader(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: RequestHeaderId = 0,
    @ColumnInfo(name = "shortcut_id", index = true)
    val shortcutId: ShortcutId,
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: String,
    @ColumnInfo(name = "sorting_order", index = true)
    val sortingOrder: Int = 0,
)
