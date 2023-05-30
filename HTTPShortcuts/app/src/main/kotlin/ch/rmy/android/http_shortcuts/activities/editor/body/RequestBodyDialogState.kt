package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.runtime.Stable
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
        var useImageEditor: Boolean,
    ) : RequestBodyDialogState()
}
