package ch.rmy.android.http_shortcuts.activities.editor.response

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class ResponseViewState(
    val variables: List<VariableModel>? = null,
    val successMessageHint: Localizable = Localizable.EMPTY,
    val responseUiType: String = "",
    val responseSuccessOutput: String = "",
    val responseFailureOutput: String = "",
    val includeMetaInformation: Boolean = false,
    val successMessage: String = "",
) {
    private val hasOutput
        get() = responseSuccessOutput != ResponseHandlingModel.SUCCESS_OUTPUT_NONE ||
            responseFailureOutput != ResponseHandlingModel.FAILURE_OUTPUT_NONE

    val responseUiTypeVisible
        get() = hasOutput

    val successMessageVisible
        get() = responseSuccessOutput == ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE

    val includeMetaInformationVisible
        get() = responseUiType == ResponseHandlingModel.UI_TYPE_WINDOW && hasOutput
}
