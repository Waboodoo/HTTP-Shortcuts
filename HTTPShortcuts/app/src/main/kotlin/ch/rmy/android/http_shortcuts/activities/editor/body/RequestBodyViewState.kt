package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil

@Stable
data class RequestBodyViewState(
    val dialogState: RequestBodyDialogState? = null,
    val requestBodyType: RequestBodyType,
    val fileUploadType: FileUploadType,
    val parameters: List<ParameterListItem>,
    val contentType: String,
    val bodyContent: String,
    val bodyContentError: String = "",
    val useImageEditor: Boolean,
    val fileName: String?,
) {
    val addParameterButtonVisible: Boolean
        get() = requestBodyType == RequestBodyType.FORM_DATA ||
            requestBodyType == RequestBodyType.X_WWW_FORM_URLENCODE

    val syntaxHighlightingLanguage: String?
        get() = when (contentType) {
            FileTypeUtil.TYPE_JSON -> "json"
            FileTypeUtil.TYPE_XML,
            FileTypeUtil.TYPE_XML_ALT,
            -> "xml"
            FileTypeUtil.TYPE_HTML -> "html"
            else -> null
        }
}
