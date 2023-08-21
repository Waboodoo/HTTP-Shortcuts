package ch.rmy.android.http_shortcuts.activities.editor.body

import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Stable
sealed class RequestBodyDialogState {
    @Stable
    object ParameterTypePicker : RequestBodyDialogState()

    @Stable
    data class ParameterEditor(
        val id: String?,
        val key: String,
        val value: String,
        val fileName: String,
        val type: ParameterType,
        val useImageEditor: Boolean = false,
        val fileUploadType: FileUploadType = FileUploadType.FILE_PICKER,
        val sourceFile: Uri? = null,
        val sourceFileName: String? = null,
    ) : RequestBodyDialogState()
}
