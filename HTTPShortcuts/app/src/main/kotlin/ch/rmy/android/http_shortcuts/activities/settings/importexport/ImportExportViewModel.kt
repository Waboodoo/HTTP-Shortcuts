package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings

class ImportExportViewModel(application: Application) : BaseViewModel<Unit, ImportExportViewState>(application), WithDialog {

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ImportExportViewState()

    fun onImportFromURLButtonClicked() {
        openImportUrlDialog()
    }

    private fun openImportUrlDialog() {
        dialogState = DialogState.create {
            title(R.string.dialog_title_import_from_url)
                .textInput(
                    prefill = Settings(context).importUrl?.toString() ?: "",
                    allowEmpty = false,
                    callback = ::startImportFromURL,
                )
                .build()
        }
    }

    private fun startImportFromURL(url: String) {
        emitEvent(ImportExportEvent.StartImportFromURL(url))
    }

    fun onImportFailedDueToInvalidUrl() {
        dialogState = DialogState.create {
            message(context.getString(R.string.import_failed_with_reason, context.getString(R.string.error_can_only_import_from_http_url)))
                .positive(R.string.dialog_ok)
                .build()
        }
    }

    fun onImportFailed(message: String) {
        dialogState = DialogState.create {
            message(context.getString(R.string.import_failed_with_reason, message))
                .positive(R.string.dialog_ok)
                .build()
        }
    }
}
