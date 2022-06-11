package ch.rmy.android.http_shortcuts.scripting

sealed interface ActionRequest {
    object ScanBarcode : ActionRequest
}
