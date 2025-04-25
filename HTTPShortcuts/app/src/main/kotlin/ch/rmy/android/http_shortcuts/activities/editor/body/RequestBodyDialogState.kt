package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Stable
sealed class RequestBodyDialogState {
    @Stable
    data object ParameterTypePicker : RequestBodyDialogState()

    @Stable
    data class ParameterEditor(
        val id: RequestParameterId?,
        val key: String,
        val value: String,
        val fileName: String,
        val type: ParameterType,
        val useImageEditor: Boolean = false,
        val fileUploadType: FileUploadType = FileUploadType.FILE_PICKER,
        val sourceDirectoryId: WorkingDirectoryId? = null,
        val sourceDirectoryName: String? = null,
        val sourceFileName: String,
        val fileNameSuggestions: List<String>,
    ) : RequestBodyDialogState()
}
