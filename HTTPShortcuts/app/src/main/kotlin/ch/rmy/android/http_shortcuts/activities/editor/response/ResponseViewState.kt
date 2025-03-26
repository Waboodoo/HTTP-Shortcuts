package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType

@Stable
data class ResponseViewState(
    val successMessageHint: Localizable,
    val responseUiType: ResponseUiType,
    val responseSuccessOutput: ResponseSuccessOutput,
    val responseFailureOutput: ResponseFailureOutput,
    val successMessage: String,
    val responseCharset: String?,
    val availableCharsets: List<String>,
    val storeResponseIntoFile: Boolean,
    val storeDirectoryName: String?,
    val storeFileName: String,
    val replaceFileIfExists: Boolean,
)
