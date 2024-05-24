package ch.rmy.android.http_shortcuts.activities.response

import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.activities.response.models.TableData
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction

@Stable
data class DisplayResponseViewState(
    val actions: List<ResponseDisplayAction>,
    val detailInfo: DetailInfo?,
    val monospace: Boolean,
    val fontSize: Int?,
    val text: String,
    val fileUri: Uri?,
    val limitExceeded: Long?,
    val mimeType: String?,
    val url: Uri?,
    val canShare: Boolean,
    val canCopy: Boolean,
    val canSave: Boolean,
    val isSaving: Boolean = false,
    val showExternalUrlWarning: Boolean,
    val tableData: TableData? = null,
    val processing: Boolean = false,
)
