package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.text.Html.escapeHtml
import androidx.annotation.StringRes
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.remote_edit.RemoteEditActivity.Companion.EXTRA_CHANGES_IMPORTED
import ch.rmy.android.http_shortcuts.http.HttpClients
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.StringUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class RemoteEditViewModel(application: Application) : BaseViewModel<Unit, RemoteEditViewState>(application), WithDialog {

    private val settings = Settings(context)

    private var disposable: Disposable? = null

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
        get() = currentViewState.dialogState
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
                    .map(HTMLUtil::getHTML)
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
        if (currentViewState.canUpload) {
            startUpload()
        }
    }

    private fun startUpload() {
        getRemoteEditManager()
            .upload(deviceId, password)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                showProgressDialog(R.string.remote_edit_upload_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                {
                    showSnackbar(R.string.message_remote_edit_upload_successful)
                },
                { error ->
                    logException(error)
                    showMessageDialog(R.string.error_remote_edit_upload)
                }
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    fun onDownloadButtonClicked() {
        if (currentViewState.canDownload) {
            startDownload()
        }
    }

    private fun startDownload() {
        getRemoteEditManager()
            .download(deviceId, password)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                showProgressDialog(R.string.remote_edit_download_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                {
                    setResult(
                        result = Activity.RESULT_OK,
                        intent = createIntent {
                            putExtra(EXTRA_CHANGES_IMPORTED, true)
                        },
                    )
                    showSnackbar(R.string.message_remote_edit_download_successful)
                },
                { error ->
                    if (error is ImportException) {
                        showMessageDialog(context.getString(R.string.error_remote_edit_download) + " " + error.message)
                    } else {
                        logException(error)
                        showMessageDialog(R.string.error_remote_edit_download)
                    }
                }
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun getRemoteEditManager() =
        RemoteEditManager(
            context = context,
            client = HttpClients.getClient(context),
            baseUrl = getRemoteBaseUrl()
                .buildUpon()
                .appendEncodedPath(REMOTE_API_PATH)
                .build(),
            exporter = Exporter(context),
            importer = Importer(context),
        )

    private fun showProgressDialog(message: Int) {
        dialogState = object : DialogState {
            override val id = PROGRESS_DIALOG_ID
            override fun createDialog(context: Context, viewModel: WithDialog): Dialog =
                ProgressDialog(context).apply {
                    setMessage(context.getString(message))
                    setCanceledOnTouchOutside(false)
                    setOnCancelListener {
                        onProgressDialogCanceled()
                    }
                }
        }
    }

    private fun hideProgressDialog() {
        if (dialogState?.id == PROGRESS_DIALOG_ID) {
            dialogState = null
        }
    }

    private fun onProgressDialogCanceled() {
        disposable?.dispose()
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

        private const val PROGRESS_DIALOG_ID = "progress"

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
