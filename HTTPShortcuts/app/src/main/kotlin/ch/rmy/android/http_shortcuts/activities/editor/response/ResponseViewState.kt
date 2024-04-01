package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import java.nio.charset.Charset

@Stable
data class ResponseViewState(
    val dialogState: ResponseDialogState? = null,
    val successMessageHint: Localizable,
    val responseUiType: String,
    val responseContentType: ResponseContentType?,
    val responseCharset: Charset?,
    val availableCharsets: List<Charset>,
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
    val fontSize: Int?,
)
