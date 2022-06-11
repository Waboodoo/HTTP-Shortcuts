package ch.rmy.android.http_shortcuts.scripting

sealed interface ActionResult {
    sealed interface ScanBarcodeResult : ActionResult {
        data class Success(val data: String) : ScanBarcodeResult
        object ScannerAppNotInstalled : ScanBarcodeResult
    }

    object Cancelled : ActionResult
}
