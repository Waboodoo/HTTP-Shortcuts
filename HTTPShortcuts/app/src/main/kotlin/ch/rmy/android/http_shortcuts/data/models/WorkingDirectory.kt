package ch.rmy.android.http_shortcuts.data.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import java.time.Instant

@Entity(tableName = "working_directory")
data class WorkingDirectory(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: WorkingDirectoryId,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "directory")
    val directory: Uri,
    @ColumnInfo(name = "accessed")
    val accessed: Instant?,
)
