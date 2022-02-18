package ch.rmy.android.http_shortcuts.activities.editor.response

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Variable

data class ResponseViewState(
    val variables: List<Variable>? = null,
    val successMessageHint: Localizable = Localizable.EMPTY,
    val responseUiType: String = "",
    val responseSuccessOutput: String = "",
    val responseFailureOutput: String = "",
    val includeMetaInformation: Boolean = false,
    val successMessage: String = "",
) {
    private val hasOutput
        get() = responseSuccessOutput != ResponseHandling.SUCCESS_OUTPUT_NONE ||
            responseFailureOutput != ResponseHandling.FAILURE_OUTPUT_NONE

    val responseUiTypeVisible
        get() = hasOutput

    val successMessageVisible
        get() = responseSuccessOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE

    val includeMetaInformationVisible
        get() = responseUiType == ResponseHandling.UI_TYPE_WINDOW && hasOutput
}
