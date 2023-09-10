package ch.rmy.android.http_shortcuts.activities.response

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.readIntoString
import ch.rmy.android.http_shortcuts.http.HttpStatus
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil.isImage
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.time.Duration

@HiltViewModel
class DisplayResponseViewModel
@Inject
constructor(
    application: Application,
    private val clipboardUtil: ClipboardUtil,
    private val activityProvider: ActivityProvider,
    private val shareUtil: ShareUtil,
) : BaseViewModel<DisplayResponseViewModel.InitData, DisplayResponseViewState>(application) {

    private lateinit var responseText: String
    private var responseTooLarge: Boolean = false

    private var savingJob: Job? = null

    override suspend fun initialize(data: InitData): DisplayResponseViewState {
        logInfo("Preparing to display response of type ${data.mimeType}")
        responseText = if (isImage(data.mimeType)) {
            ""
        } else {
            data.text
                ?: try {
                    withContext(Dispatchers.IO) {
                        data.fileUri?.readIntoString(context, CONTENT_SIZE_LIMIT, data.charset)
                            ?.runIf(initData.mimeType == FileTypeUtil.TYPE_JSON) {
                                GsonUtil.tryPrettyPrint(this)
                            }
                            ?: ""
                    }
                } catch (e: SizeLimitedReader.LimitReachedException) {
                    logInfo("Response is too large")
                    responseTooLarge = true
                    ""
                }
        }
        return DisplayResponseViewState(
            detailInfo = if (initData.showDetails) {
                DetailInfo(
                    url = initData.url?.toString(),
                    status = initData.statusCode?.let { "$it (${HttpStatus.getMessage(it)})" },
                    timing = initData.timing,
                    headers = initData.headers.entries.flatMap { (key, values) -> values.map { key to it } },
                )
                    .takeUnless { !it.hasGeneralInfo && it.headers.isEmpty() }
            } else null,
            monospace = initData.monospace,
            text = responseText,
            fileUri = initData.fileUri,
            limitExceeded = if (responseTooLarge) CONTENT_SIZE_LIMIT else null,
            mimeType = initData.mimeType,
            url = initData.url,
            canShare = initData.fileUri != null,
            canCopy = responseText.isNotEmpty() && responseText.length < MAX_COPY_LENGTH,
            canSave = initData.fileUri != null,
        )
    }

    fun onRerunButtonClicked() = runAction {
        sendIntent(
            ExecuteActivity.IntentBuilder(initData.shortcutId)
                .trigger(ShortcutTriggerType.WINDOW_RERUN)
        )
        finish(skipAnimation = true)
    }

    fun onShareButtonClicked() = runAction {
        if (shouldShareAsText()) {
            shareUtil.shareText(activityProvider.getActivity(), responseText)
        } else {
            sendIntent(
                Intent(Intent.ACTION_SEND)
                    .runIfNotNull(initData.mimeType) {
                        setType(it)
                    }
                    .putExtra(Intent.EXTRA_STREAM, initData.fileUri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .let {
                        Intent.createChooser(it, initData.shortcutName)
                    }
            )
        }
    }

    private fun shouldShareAsText() =
        !isImage(initData.mimeType) && responseText.length < MAX_SHARE_LENGTH

    fun onCopyButtonClicked() {
        clipboardUtil.copyToClipboard(responseText)
    }

    fun onSaveButtonClicked() = runAction {
        emitEvent(DisplayResponseEvent.SuppressAutoFinish)
        emitEvent(DisplayResponseEvent.PickFileForSaving(initData.mimeType))
    }

    fun onFilePickedForSaving(file: Uri) = runAction {
        updateViewState {
            copy(isSaving = true)
        }
        savingJob = launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(file)!!.use { output ->
                        context.contentResolver.openInputStream(initData.fileUri!!)!!.use { input ->
                            input.copyTo(output)
                        }
                    }
                }
                showSnackbar(R.string.message_response_saved_to_file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showSnackbar(R.string.error_generic)
                logException(e)
            } finally {
                updateViewState {
                    copy(isSaving = false)
                }
            }
        }
    }

    fun onDialogDismissed() = runAction {
        savingJob?.cancel()
        updateViewState {
            copy(isSaving = false)
        }
    }

    fun onFilePickerFailed() = runAction {
        showSnackbar(R.string.error_not_supported)
    }

    @Stable
    data class InitData(
        val shortcutId: ShortcutId,
        val shortcutName: String,
        val text: String?,
        val mimeType: String?,
        val charset: Charset,
        val url: Uri?,
        val fileUri: Uri?,
        val statusCode: Int?,
        val headers: Map<String, List<String>>,
        val timing: Duration?,
        val showDetails: Boolean,
        val monospace: Boolean,
        val actions: List<ResponseDisplayAction>,
    )

    companion object {
        private const val MAX_SHARE_LENGTH = 200000
        private const val MAX_COPY_LENGTH = 200000
        private const val CONTENT_SIZE_LIMIT = 1000L * 1000L
    }
}
