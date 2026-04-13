package ch.rmy.android.http_shortcuts.import_export.models

import androidx.annotation.Keep
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId

@Keep
data class ImportExportFileUploadOptions(
    val fileUploadType: String? = null,
    val directoryId: WorkingDirectoryId? = null,
    val fileName: String? = null,
    val value: String? = null,
    val useImageEditor: Boolean? = null,
) {
    fun validate() {
        require(fileName == null || !fileName.contains("/")) {
            "Invalid parameter file upload file name: $fileName"
        }
    }
}

typealias ImportFileUploadOptions = ImportExportFileUploadOptions

typealias ExportFileUploadOptions = ImportExportFileUploadOptions
