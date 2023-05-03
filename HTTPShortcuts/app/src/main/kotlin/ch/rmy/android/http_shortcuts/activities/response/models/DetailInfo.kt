package ch.rmy.android.http_shortcuts.activities.response.models

import androidx.compose.runtime.Stable
import kotlin.time.Duration

@Stable
data class DetailInfo(
    val url: String?,
    val status: String?,
    val timing: Duration?,
    val headers: List<Pair<String, String>>,
) {
    val hasGeneralInfo = url != null || status != null || timing != null
}
