package ch.rmy.android.http_shortcuts.activities.response

import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo

@Stable
data class DisplayResponseViewState(
    val detailInfo: DetailInfo?,
    val text: String,
    val fileUri: Uri?,
    val limitExceeded: Long?,
    val mimeType: String?,
    val url: Uri?,
    val canShare: Boolean,
    val canCopy: Boolean,
    val canSave: Boolean,
    val isSaving: Boolean = false,
)
