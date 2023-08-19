package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction

@Stable
data class ResponseViewState(
    val successMessageHint: Localizable,
    val responseUiType: String,
    val responseSuccessOutput: String,
    val responseFailureOutput: String,
    val includeMetaInformation: Boolean,
    val successMessage: String,
    val responseDisplayActions: List<ResponseDisplayAction>,
    val storeResponseIntoFile: Boolean,
    val storeDirectory: String?,
    val storeFileName: String,
    val replaceFileIfExists: Boolean,
    val useMonospaceFont: Boolean,
)
