package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import java.nio.charset.Charset

@Stable
data class ResponseDisplayViewState(
    val dialogState: ResponseDisplayDialogState? = null,
    val responseUiType: String,
    val responseSuccessOutput: String,
    val responseContentType: ResponseContentType?,
    val responseCharset: Charset?,
    val availableCharsets: List<Charset>,
    val useMonospaceFont: Boolean,
    val fontSize: Int?,
    val includeMetaInformation: Boolean,
    val responseDisplayActions: List<ResponseDisplayAction>,
    val jsonArrayAsTable: Boolean,
)
