package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import java.time.Instant

@Entity(tableName = "pending_execution")
data class PendingExecutionModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: ExecutionId = 0,
    @ColumnInfo(name = "shortcut_id")
    val shortcutId: ShortcutId,
    @ColumnInfo(name = "try_number")
    val tryNumber: Int,
    @ColumnInfo(name = "delay_until")
    val delayUntil: Instant?,
    @ColumnInfo(name = "wait_for_network")
    val waitForNetwork: Boolean,
    @ColumnInfo(name = "recursion_depth")
    val recursionDepth: Int,
    @ColumnInfo(name = "request_code")
    val requestCode: Int,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "enqueued_at")
    val enqueuedAt: Instant,
)
