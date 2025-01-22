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
    val title: String,
    val text: String? = null,
    val mimeType: String? = null,
    val charset: Charset? = null,
    val url: Uri? = null,
    val fileUri: Uri? = null,
    val statusCode: Int? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val timing: Duration? = null,
    val showDetails: Boolean = false,
    val monospace: Boolean = false,
    val fontSize: Int? = null,
    val actions: List<ResponseDisplayAction> = emptyList(),
    val jsonArrayAsTable: Boolean = false,
    val javaScriptEnabled: Boolean = false,
)
