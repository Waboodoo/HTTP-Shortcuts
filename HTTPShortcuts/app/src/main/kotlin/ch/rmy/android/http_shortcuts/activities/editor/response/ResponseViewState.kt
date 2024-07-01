package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable

@Stable
data class ResponseViewState(
    val successMessageHint: Localizable,
    val responseUiType: String,
    val responseSuccessOutput: String,
    val responseFailureOutput: String,
    val successMessage: String,
    val storeResponseIntoFile: Boolean,
    val storeDirectoryName: String?,
    val storeFileName: String,
    val replaceFileIfExists: Boolean,
)
