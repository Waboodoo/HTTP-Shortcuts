package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ActionRequest
import ch.rmy.android.http_shortcuts.scripting.ActionResult
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class ScanBarcodeAction : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): String? =
        when (val result = executionContext.sendRequest(ActionRequest.ScanBarcode)) {
            is ActionResult.ScanBarcodeResult.Success -> result.data
            is ActionResult.ScanBarcodeResult.ScannerAppNotInstalled -> throw ActionException { context ->
                context.getString(R.string.error_barcode_scanner_not_installed)
            }
            is ActionResult.Cancelled -> null
        }
}
