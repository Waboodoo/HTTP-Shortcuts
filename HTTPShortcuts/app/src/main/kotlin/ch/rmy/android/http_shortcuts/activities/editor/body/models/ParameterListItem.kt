package ch.rmy.android.http_shortcuts.activities.editor.body.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterId
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType

@Stable
data class ParameterListItem(
    val id: RequestParameterId,
    val key: String,
    val value: String,
    val type: ParameterType,
    val fileUploadType: FileUploadType?,
)
