package ch.rmy.android.http_shortcuts.activities.execute

sealed interface ExternalRequest {
    data class PickFiles(val multiple: Boolean) : ExternalRequest
    object OpenCamera : ExternalRequest
    object ScanBarcode : ExternalRequest
}
