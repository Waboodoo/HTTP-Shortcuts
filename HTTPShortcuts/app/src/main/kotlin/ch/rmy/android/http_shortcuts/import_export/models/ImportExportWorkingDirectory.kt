package ch.rmy.android.http_shortcuts.import_export.models

import androidx.annotation.Keep
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId

@Keep
data class ImportExportWorkingDirectory(
    val id: WorkingDirectoryId? = null,
    val name: String? = null,
    val directory: String? = null,
) {
    fun validate() {
        require(id == null || id.isUUID()) {
            "Invalid directory ID found, must be UUID: $id"
        }
        require(!name.isNullOrEmpty()) {
            "Invalid directory name for working directory"
        }
        require(directory?.startsWith("content://", ignoreCase = true) == true) {
            "Invalid directory URI for working directory"
        }
    }
}

typealias ImportWorkingDirectory = ImportExportWorkingDirectory

typealias ExportWorkingDirectory = ImportExportWorkingDirectory
