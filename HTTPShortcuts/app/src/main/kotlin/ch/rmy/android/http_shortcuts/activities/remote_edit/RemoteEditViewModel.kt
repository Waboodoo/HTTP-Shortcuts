package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Application
import android.text.Html.escapeHtml
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.framework.viewmodel.viewstate.ProgressDialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.StringUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class RemoteEditViewModel(application: Application) : BaseViewModel<Unit, RemoteEditViewState>(application), WithDialog {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var exporter: Exporter

    @Inject
    lateinit var importer: Importer

    @Inject
    lateinit var httpClientFactory: HttpClientFactory

    init {
        getApplicationComponent().inject(this)
    }

    private var currentJob: Job? = null

    private var serverUrl: String
        get() = settings.remoteEditServerUrl ?: REMOTE_BASE_URL
        set(value) {
            settings.remoteEditServerUrl = value
            updateViewState {
                copy(instructions = getInstructions())
            }
        }

    private val humanReadableEditorAddress: String
        get() = getRemoteBaseUrl().toString().replace("https://", "")

    private val deviceId: String
        get() = settings.remoteEditDeviceId
            ?: run {
                generateDeviceId()
                    .also {
                        settings.remoteEditDeviceId = it
                    }
            }

    private var password: String
        get() = settings.remoteEditPassword ?: ""
        set(value) {
            settings.remoteEditPassword = value
            updateViewState {
                copy(password = value)
            }
        }

    private fun getRemoteBaseUrl() =
        serverUrl.toUri()

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = RemoteEditViewState(
        instructions = getInstructions(),
        deviceId = deviceId,
        password = password,
    )

    private fun getInstructions() =
        Localizable.create { context ->
            StringUtils.getOrderedList(
                listOf(
                    context.getString(R.string.instructions_remote_edit_step_1),
                    context.getString(R.string.instructions_remote_edit_step_2),
                    context.getString(R.string.instructions_remote_edit_step_3, "<b>${escapeHtml(humanReadableEditorAddress)}</b>"),
                    context.getString(R.string.instructions_remote_edit_step_4),
                )
                    .map(HTMLUtil::format)
            )
        }

    fun onChangeRemoteHostButtonClicked() {
        openChangeRemoteHostDialog()
    }

    private fun openChangeRemoteHostDialog() {
        dialogState = DialogState.create {
            title(R.string.title_change_remote_server)
                .textInput(
                    prefill = serverUrl,
                    allowEmpty = false,
                    callback = ::setRemoteHost,
                )
                .neutral(R.string.dialog_reset) {
                    onResetRemoteHostButtonClicked()
                }
                .build()
        }
    }

    private fun onResetRemoteHostButtonClicked() {
        setRemoteHost("")
    }

    private fun setRemoteHost(value: String) {
        if (value.isNotEmpty() && !Validation.isValidHttpUrl(value.toUri())) {
            showMessageDialog(R.string.error_invalid_remote_edit_host_url)
            return
        }
        serverUrl = value
    }

    fun onPasswordChanged(password: String) {
        this.password = password
    }

    fun onUploadButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.canUpload) {
                startUpload()
            }
        }
    }

    private fun startUpload() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                showProgressDialog(R.string.remote_edit_upload_in_progress)
                getRemoteEditManager().upload(deviceId, password)
                showSnackbar(R.string.message_remote_edit_upload_successful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                showMessageDialog(R.string.error_remote_edit_upload)
            } finally {
                hideProgressDialog()
            }
        }
    }

    fun onDownloadButtonClicked() {
        doWithViewState { viewState ->
            if (viewState.canDownload) {
                startDownload()
            }
        }
    }

    private fun startDownload() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                showProgressDialog(R.string.remote_edit_download_in_progress)
                getRemoteEditManager().download(deviceId, password)
                setResult(
                    intent = RemoteEditActivity.OpenRemoteEditor.createResult(changesImported = true),
                )
                showSnackbar(R.string.message_remote_edit_download_successful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ImportException) {
                showMessageDialog(context.getString(R.string.error_remote_edit_download) + " " + e.message)
            } catch (e: Exception) {
                logException(e)
                showMessageDialog(R.string.error_remote_edit_download)
            } finally {
                hideProgressDialog()
            }
        }
    }

    private fun getRemoteEditManager() =
        RemoteEditManager(
            context = context,
            client = httpClientFactory.getClient(context),
            baseUrl = getRemoteBaseUrl()
                .buildUpon()
                .appendEncodedPath(REMOTE_API_PATH)
                .build(),
            exporter = exporter,
            importer = importer,
        )

    private fun showProgressDialog(message: Int) {
        dialogState = ProgressDialogState(StringResLocalizable(message), ::onProgressDialogCanceled)
    }

    private fun hideProgressDialog() {
        if (dialogState?.id == ProgressDialogState.DIALOG_ID) {
            dialogState = null
        }
    }

    private fun onProgressDialogCanceled() {
        currentJob?.cancel()
    }

    private fun showMessageDialog(@StringRes message: Int) {
        dialogState = DialogState.create {
            message(message)
                .positive(R.string.dialog_ok)
                .build()
        }
    }

    private fun showMessageDialog(message: String) {
        dialogState = DialogState.create {
            message(message)
                .positive(R.string.dialog_ok)
                .build()
        }
    }

    companion object {

        private const val REMOTE_BASE_URL = "https://http-shortcuts.rmy.ch/editor"
        private const val REMOTE_API_PATH = "api/files/"

        private const val DEVICE_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val DEVICE_ID_LENGTH = 8

        private fun generateDeviceId(): String =
            (0 until DEVICE_ID_LENGTH)
                .map {
                    DEVICE_ID_CHARACTERS.random()
                }
                .joinToString(separator = "")
    }
}
