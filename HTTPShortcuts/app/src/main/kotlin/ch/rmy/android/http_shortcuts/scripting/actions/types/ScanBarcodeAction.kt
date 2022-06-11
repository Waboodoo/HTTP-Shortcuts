package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ActionRequest
import ch.rmy.android.http_shortcuts.scripting.ActionResult
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ScanBarcodeAction : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        executionContext.sendRequest(ActionRequest.ScanBarcode)
            .observeOn(Schedulers.io())
            .map { result ->
                when (result) {
                    is ActionResult.ScanBarcodeResult.Success -> result.data
                    is ActionResult.ScanBarcodeResult.ScannerAppNotInstalled -> throw ActionException { context ->
                        context.getString(R.string.error_barcode_scanner_not_installed)
                    }
                    is ActionResult.Cancelled -> NO_RESULT
                }
            }
}
