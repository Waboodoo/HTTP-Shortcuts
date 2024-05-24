package ch.rmy.android.http_shortcuts.activities.response.models

import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import java.nio.charset.Charset
import kotlin.time.Duration

@Stable
data class ResponseData(
    val shortcutId: ShortcutId,
    val shortcutName: String,
    val text: String?,
    val mimeType: String?,
    val charset: Charset?,
    val url: Uri?,
    val fileUri: Uri?,
    val statusCode: Int?,
    val headers: Map<String, List<String>>,
    val timing: Duration?,
    val showDetails: Boolean,
    val monospace: Boolean,
    val fontSize: Int?,
    val actions: List<ResponseDisplayAction>,
    val jsonArrayAsTable: Boolean,
)
