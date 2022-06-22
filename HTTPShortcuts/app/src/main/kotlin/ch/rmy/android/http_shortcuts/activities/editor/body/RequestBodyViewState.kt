package ch.rmy.android.http_shortcuts.activities.editor.body

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType

data class RequestBodyViewState(
    val dialogState: DialogState? = null,
    val requestBodyType: RequestBodyType = RequestBodyType.CUSTOM_TEXT,
    val parameters: List<ParameterListItem> = emptyList(),
    val contentType: String = "",
    val bodyContent: String = "",
) {
    val isDraggingEnabled: Boolean
        get() = parameters.size > 1

    val parameterListVisible: Boolean
        get() = requestBodyType == RequestBodyType.FORM_DATA ||
            requestBodyType == RequestBodyType.X_WWW_FORM_URLENCODE

    val addParameterButtonVisible: Boolean
        get() = parameterListVisible

    val contentTypeVisible: Boolean
        get() = requestBodyType == RequestBodyType.CUSTOM_TEXT

    val bodyContentVisible: Boolean
        get() = contentTypeVisible
}
