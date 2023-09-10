package ch.rmy.android.http_shortcuts.activities.execute

import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class ExternalRequests
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {

    suspend fun scanBarcode(): String? =
        when (val result = getResult(ExternalRequest.ScanBarcode)) {
            is ExternalResult.BarcodeScanned -> result.content
            is ExternalResult.AppNotAvailable -> throw UserException.create {
                getString(R.string.error_barcode_scanner_not_installed)
            }
            is ExternalResult.Cancelled -> null
            else -> error("Unexpected result")
        }

    suspend fun openCamera(): List<Uri> =
        when (val result = getResult(ExternalRequest.OpenCamera)) {
            is ExternalResult.Files -> result.fileUris
            is ExternalResult.AppNotAvailable -> throw UserException.create {
                getString(R.string.error_not_supported)
            }
            else -> error("Unexpected result")
        }

    suspend fun cropImage(imageUri: Uri, compressFormat: CompressFormat): Uri =
        when (val result = getResult(ExternalRequest.CropImage(imageUri, compressFormat))) {
            is ExternalResult.File -> result.fileUri
            is ExternalResult.Cancelled -> throw CancellationException()
            else -> error("Unexpected result")
        }

    suspend fun openFilePicker(multiple: Boolean): List<Uri> =
        when (val result = getResult(ExternalRequest.PickFiles(multiple))) {
            is ExternalResult.Files -> result.fileUris
            is ExternalResult.AppNotAvailable -> throw UserException.create {
                getString(R.string.error_not_supported)
            }
            else -> error("Unexpected result")
        }

    private suspend fun getResult(request: ExternalRequest) =
        activityProvider.withActivity { activity ->
            ExternalRequestFragment.getResult(activity, request)
        }
}
