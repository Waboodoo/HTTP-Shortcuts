package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.http.HttpClientFactory
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.Validation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class RemoteEditViewModel(application: Application) : BaseViewModel<Unit, RemoteEditViewState>(application) {

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
            viewModelScope.launch {
                updateViewState {
                    copy(hostAddress = humanReadableEditorAddress)
                }
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
            viewModelScope.launch {
                updateViewState {
                    copy(password = value)
                }
            }
        }

    private fun getRemoteBaseUrl() =
        serverUrl.toUri()

    override suspend fun initialize(data: Unit) = RemoteEditViewState(
        hostAddress = humanReadableEditorAddress,
        deviceId = deviceId,
        password = password,
    )

    fun onChangeRemoteHostButtonClicked() = runAction {
        updateViewState {
            copy(
                dialogState = RemoteEditDialogState.EditServerUrl(
                    currentServerAddress = serverUrl,
                )
            )
        }
    }

    fun onServerUrlChange(value: String) = runAction {
        if (value.isNotEmpty() && !Validation.isValidHttpUrl(value.toUri())) {
            showErrorDialog(StringResLocalizable(R.string.error_invalid_remote_edit_host_url))
            skipAction()
        }
        serverUrl = value
        hideDialog()
    }

    fun onPasswordChanged(password: String) = runAction {
        this@RemoteEditViewModel.password = password.take(100)
    }

    fun onUploadButtonClicked() = runAction {
        if (viewState.canUpload) {
            startUpload()
        }
    }

    private fun startUpload() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val dialogJob = showProgressDialogAsync(R.string.remote_edit_upload_in_progress)
            try {
                getRemoteEditManager().upload(deviceId, password)
                showSnackbar(R.string.message_remote_edit_upload_successful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logException(e)
                showErrorDialog(StringResLocalizable(R.string.error_remote_edit_upload))
            } finally {
                dialogJob.cancel()
                hideProgressDialog()
            }
        }
    }

    fun onDownloadButtonClicked() = runAction {
        if (viewState.canDownload) {
            startDownload()
        }
    }

    private fun startDownload() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val dialogJob = showProgressDialogAsync(R.string.remote_edit_download_in_progress)
            try {
                getRemoteEditManager().download(deviceId, password)
                setResult(
                    intent = RemoteEditActivity.OpenRemoteEditor.createResult(changesImported = true),
                )
                showSnackbar(R.string.message_remote_edit_download_successful)
            } catch (e: CancellationException) {
                throw e
            } catch (e: ImportException) {
                showErrorDialog(Localizable.create { it.getString(R.string.error_remote_edit_download) + " " + e.message })
            } catch (e: Exception) {
                logException(e)
                showErrorDialog(StringResLocalizable(R.string.error_remote_edit_download))
            } finally {
                dialogJob.cancel()
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

    private fun CoroutineScope.showProgressDialogAsync(message: Int): Deferred<Unit> =
        async {
            delay(INVISIBLE_PROGRESS_THRESHOLD)
            updateViewState {
                copy(dialogState = RemoteEditDialogState.Progress(StringResLocalizable(message)))
            }
        }

    private suspend fun hideProgressDialog() {
        updateViewState {
            if (dialogState is RemoteEditDialogState.Progress) {
                copy(dialogState = null)
            } else this
        }
    }

    fun onDialogDismissalRequested() = runAction {
        currentJob?.cancel()
        hideDialog()
    }

    private suspend fun hideDialog() {
        updateViewState {
            copy(dialogState = null)
        }
    }

    private suspend fun showErrorDialog(message: Localizable) {
        updateViewState {
            copy(
                dialogState = RemoteEditDialogState.Error(message),
            )
        }
    }

    companion object {

        private val INVISIBLE_PROGRESS_THRESHOLD = 400.milliseconds

        private const val REMOTE_BASE_URL = "https://http-shortcuts.rmy.ch/editor"
        private const val REMOTE_API_PATH = "api/files/"

        private const val DEVICE_ID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val DEVICE_ID_LENGTH = 8

        internal fun generateDeviceId(): String =
            (0 until DEVICE_ID_LENGTH)
                .map {
                    DEVICE_ID_CHARACTERS.random()
                }
                .joinToString(separator = "")
    }
}
