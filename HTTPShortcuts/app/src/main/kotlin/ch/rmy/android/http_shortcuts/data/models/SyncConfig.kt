package ch.rmy.android.http_shortcuts.data.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import java.time.Instant

@Entity(tableName = "sync_config")
data class SyncConfig(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "sync_type")
    val type: SyncType,
    @ColumnInfo(name = "target_type")
    val targetType: SyncTargetType = SyncTargetType.FILE,
    @ColumnInfo(name = "target_directory_uri")
    val targetDirectoryUri: Uri? = null,
    @ColumnInfo(name = "target_file_name")
    val targetFileName: String? = null,
    @ColumnInfo(name = "target_url")
    val targetUrl: String? = null,
    @ColumnInfo(name = "target_auth_username")
    val targetAuthUsername: String? = null,
    @ColumnInfo(name = "target_auth_password")
    val targetAuthPassword: String? = null,
    @ColumnInfo(name = "schedule")
    val schedule: SyncSchedule,
    @ColumnInfo(name = "file_password")
    val filePassword: String = "",
    @ColumnInfo(name = "replace_local")
    val replaceLocal: Boolean = false,
    @ColumnInfo(name = "last_succeeded")
    val lastSucceeded: Instant? = null,
    @ColumnInfo(name = "last_failed")
    val lastFailed: Instant? = null,
    @ColumnInfo(name = "category_ids", defaultValue = "")
    val categoryIdsString: String = "",
) {
    val isValid: Boolean
        get() = when (targetType) {
            SyncTargetType.FILE -> targetDirectoryUri != null
            SyncTargetType.URL -> !targetUrl.isNullOrEmpty()
        }

    val categoryIds: Set<CategoryId>?
        get() = categoryIdsString.takeUnlessEmpty()?.split(',')?.toSet()

    companion object {
        const val DEFAULT_FILE_NAME = "shortcuts-%Y-%M-%D.zip"
    }
}
