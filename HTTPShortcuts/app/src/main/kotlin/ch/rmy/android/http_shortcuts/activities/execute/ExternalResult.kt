package ch.rmy.android.http_shortcuts.activities.execute

import android.net.Uri

sealed interface ExternalResult {
    data class Files(val fileUris: List<Uri>) : ExternalResult
    data class File(val fileUri: Uri) : ExternalResult
    data class BarcodeScanned(val content: String) : ExternalResult
    data object AppNotAvailable : ExternalResult
    data object Cancelled : ExternalResult
}
