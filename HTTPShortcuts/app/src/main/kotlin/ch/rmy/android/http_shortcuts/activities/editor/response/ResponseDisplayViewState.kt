package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType

@Stable
data class ResponseDisplayViewState(
    val dialogState: ResponseDisplayDialogState? = null,
    val responseUiType: ResponseUiType,
    val responseSuccessOutput: ResponseSuccessOutput,
    val responseContentType: ResponseContentType?,
    val useMonospaceFont: Boolean,
    val fontSize: Int?,
    val includeMetaInformation: Boolean,
    val responseDisplayActions: List<ResponseDisplayAction>,
    val jsonArrayAsTable: Boolean,
    val javaScriptEnabled: Boolean,
)
