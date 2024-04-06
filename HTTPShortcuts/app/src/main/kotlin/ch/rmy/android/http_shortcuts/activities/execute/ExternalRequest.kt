package ch.rmy.android.http_shortcuts.activities.execute

import android.graphics.Bitmap
import android.net.Uri

sealed interface ExternalRequest {
    data class PickFiles(val multiple: Boolean) : ExternalRequest
    data object OpenCamera : ExternalRequest
    data class CropImage(val imageUri: Uri, val compressFormat: Bitmap.CompressFormat) : ExternalRequest
    data object ScanBarcode : ExternalRequest
}
