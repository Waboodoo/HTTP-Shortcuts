package ch.rmy.android.http_shortcuts.activities.execute

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.framework.utils.FileUtil.getUriFromFile
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.CropImageContract
import ch.rmy.android.http_shortcuts.utils.BarcodeScannerContract
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExternalRequestFragment : Fragment() {

    private val pickFiles = registerForActivityResult(FilePickerUtil.PickFiles) { files ->
        if (files != null) {
            returnResult(ExternalResult.Files(fileUris = files))
        } else {
            cancel()
        }
    }
    private val openCamera = registerForActivityResult(FilePickerUtil.OpenCamera) { resultCallback ->
        resultCallback.invoke(requireContext())
            ?.let { file ->
                returnResult(ExternalResult.Files(fileUris = listOf(file)))
            }
            ?: cancel()
    }
    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        returnResult(
            when (result) {
                is CropImageContract.Result.Success -> ExternalResult.File(fileUri = getUriFromFile(requireContext(), result.imageFile))
                else -> ExternalResult.Cancelled
            },
        )
    }
    private val scanBarcode = registerForActivityResult(BarcodeScannerContract) { result ->
        returnResult(
            if (result != null) {
                ExternalResult.BarcodeScanned(result)
            } else {
                ExternalResult.Cancelled
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (savedInstanceState == null) {
                logInfo("Handling external request: $request")
                when (val request = request) {
                    is ExternalRequest.PickFiles -> pickFiles.launch(request.multiple)
                    is ExternalRequest.OpenCamera -> openCamera.launch()
                    is ExternalRequest.CropImage -> cropImage.launch(CropImageContract.Input(request.imageUri, request.compressFormat))
                    is ExternalRequest.ScanBarcode -> scanBarcode.launch()
                    null -> error("Request was not set")
                }
            }
        } catch (e: ActivityNotFoundException) {
            logInfo("Activity not found for external request: $e")
            returnResult(ExternalResult.AppNotAvailable)
        }
    }

    private fun returnResult(result: ExternalResult) {
        removeSelf()
        if (deferred == null) {
            context?.showToast(R.string.error_generic)
            logException(IllegalStateException("Failed to return result from external app, process was restarted"))
            return
        }
        deferred!!.complete(result)
        deferred = null
        request = null
    }

    private fun cancel() {
        removeSelf()
        if (deferred == null) {
            context?.showToast(R.string.error_generic)
            logException(IllegalStateException("Failed to cancel from external app, process was restarted"))
            return
        }
        deferred!!.cancel()
        deferred = null
        request = null
    }

    private fun removeSelf() {
        requireActivity().supportFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        logInfo("ExternalRequestFragment destroyed")
    }

    companion object {

        private const val TAG = "ExternalRequestFragment"

        private var request: ExternalRequest? = null
        private var deferred: CompletableDeferred<ExternalResult>? = null

        suspend fun getResult(activity: FragmentActivity, request: ExternalRequest): ExternalResult =
            withContext(Dispatchers.Main) {
                assert(this@Companion.request == null && deferred == null) { "A request is already in progress" }
                this@Companion.request = request
                val deferred = CompletableDeferred<ExternalResult>()
                this@Companion.deferred = deferred
                activity.supportFragmentManager
                    .beginTransaction()
                    .add(ExternalRequestFragment(), TAG)
                    .commitAllowingStateLoss()
                deferred.await()
            }
    }
}
