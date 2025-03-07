package ch.rmy.android.http_shortcuts.activities.response

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.activities.response.models.ResponseData
import ch.rmy.android.http_shortcuts.activities.response.usecases.GetTableDataUseCase
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.readIntoString
import ch.rmy.android.http_shortcuts.http.HttpStatus
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil.isImage
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DisplayResponseViewModel
@Inject
constructor(
    application: Application,
    private val navigationArgStore: NavigationArgStore,
    private val clipboardUtil: ClipboardUtil,
    private val activityProvider: ActivityProvider,
    private val shareUtil: ShareUtil,
    private val executionStarter: ExecutionStarter,
    private val settings: Settings,
    private val getTableData: GetTableDataUseCase,
) : BaseViewModel<DisplayResponseViewModel.InitData, DisplayResponseViewState>(application) {

    private lateinit var responseData: ResponseData
    private lateinit var responseText: String
    private var responseTooLarge: Boolean = false

    private var savingJob: Job? = null

    override suspend fun initialize(data: InitData): DisplayResponseViewState {
        logInfo("Preparing to display response")

        responseData = navigationArgStore.takeArg(data.responseDataId) as ResponseData?
            ?: terminateInitialization()

        responseText = if (isImage(responseData.mimeType)) {
            ""
        } else {
            responseData.text
                ?: try {
                    withContext(Dispatchers.IO) {
                        responseData.fileUri?.readIntoString(context, CONTENT_SIZE_LIMIT, responseData.charset ?: Charsets.UTF_8)
                            ?: ""
                    }
                } catch (e: SizeLimitedReader.LimitReachedException) {
                    logInfo("Response is too large")
                    responseTooLarge = true
                    ""
                }
        }

        val isJson = responseData.mimeType == FileTypeUtil.TYPE_JSON
        var processing = false

        if (isJson) {
            processing = true
            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    try {
                        val json = JsonParser.parseString(responseText)
                        val table = if (responseData.jsonArrayAsTable) {
                            getTableData(json)
                        } else {
                            null
                        }
                        updateViewState {
                            if (table != null) {
                                copy(tableData = table, processing = false)
                            } else {
                                copy(text = GsonUtil.prettyPrintOrThrow(json), processing = false)
                            }
                        }
                    } catch (e: JsonParseException) {
                        updateViewState {
                            copy(text = responseText, processing = false)
                        }
                    }
                }
            }
        }

        return DisplayResponseViewState(
            actions = responseData.actions,
            detailInfo = if (responseData.showDetails) {
                DetailInfo(
                    url = responseData.url?.toString(),
                    status = responseData.statusCode?.let { "$it (${HttpStatus.getMessage(it)})" },
                    timing = responseData.timing,
                    headers = responseData.headers.entries.flatMap { (key, values) -> values.map { key to it } },
                )
                    .takeUnless { !it.hasGeneralInfo && it.headers.isEmpty() }
            } else {
                null
            },
            monospace = responseData.monospace,
            fontSize = responseData.fontSize,
            text = responseText,
            fileUri = responseData.fileUri,
            limitExceeded = if (responseTooLarge) CONTENT_SIZE_LIMIT else null,
            mimeType = responseData.mimeType,
            url = responseData.url,
            canShare = responseData.fileUri != null,
            canCopy = responseText.isNotEmpty() && responseText.length < MAX_COPY_LENGTH,
            canSave = responseData.fileUri != null,
            showExternalUrlWarning = !settings.isExternalUrlWarningPermanentlyHidden,
            javaScriptEnabled = responseData.javaScriptEnabled,
            processing = processing,
        )
    }

    fun onRerunButtonClicked() = runAction {
        executionStarter.execute(
            shortcutId = responseData.shortcutId,
            trigger = ShortcutTriggerType.WINDOW_RERUN,
        )
        finish(skipAnimation = true)
    }

    fun onShareButtonClicked() = runAction {
        if (shouldShareAsText()) {
            activityProvider.withActivity { activity ->
                shareUtil.shareText(activity, responseText)
            }
        } else {
            sendIntent(
                Intent(Intent.ACTION_SEND)
                    .runIfNotNull(responseData.mimeType) {
                        setType(it)
                    }
                    .putExtra(Intent.EXTRA_STREAM, responseData.fileUri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .let {
                        Intent.createChooser(it, responseData.title)
                    },
            )
        }
    }

    private fun shouldShareAsText() =
        !isImage(responseData.mimeType) && responseText.length < MAX_SHARE_LENGTH

    fun onCopyButtonClicked() {
        clipboardUtil.copyToClipboard(responseText)
    }

    fun onSaveButtonClicked() = runAction {
        emitEvent(DisplayResponseEvent.SuppressAutoFinish)
        emitEvent(DisplayResponseEvent.PickFileForSaving(responseData.mimeType))
    }

    fun onFilePickedForSaving(file: Uri) = runAction {
        updateViewState {
            copy(isSaving = true)
        }
        savingJob = launch {
            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(file)!!.use { output ->
                        context.contentResolver.openInputStream(responseData.fileUri!!)!!.use { input ->
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

    fun onExternalUrlWarningHidden(hidden: Boolean) = runAction {
        updateViewState {
            copy(showExternalUrlWarning = !hidden)
        }
        settings.isExternalUrlWarningPermanentlyHidden = hidden
    }

    @Stable
    data class InitData(
        val responseDataId: NavigationArgStore.ArgStoreId,
    )

    companion object {
        private const val MAX_SHARE_LENGTH = 200000
        private const val MAX_COPY_LENGTH = 200000
        private const val CONTENT_SIZE_LIMIT = 1000L * 1000L
    }
}
