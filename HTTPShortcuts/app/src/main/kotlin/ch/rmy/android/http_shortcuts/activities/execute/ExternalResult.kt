package ch.rmy.android.http_shortcuts.activities.execute

import android.net.Uri

sealed interface ExternalResult {
    data class Files(val fileUris: List<Uri>) : ExternalResult
    data class BarcodeScanned(val content: String) : ExternalResult
    object AppNotAvailable : ExternalResult
    object Cancelled : ExternalResult
}
