package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StaticLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.framework.viewmodel.viewstate.ProgressDialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases.GetRussianWarningDialogUseCase
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.usecases.GetExportDestinationOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.disposables.Disposable

class ImportExportViewModel(application: Application) : BaseViewModel<Unit, ImportExportViewState>(application), WithDialog {

    private val settings = Settings(context)
    private val getExportDestinationOptionsDialog = GetExportDestinationOptionsDialogUseCase()
    private val exporter = Exporter(context)
    private val importer = Importer(context)
    private val getRussianWarningDialog = GetRussianWarningDialogUseCase()

    private var disposable: Disposable? = null

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
                    callback = { startImportFromURL(it.toUri()) },
                )
                .build()
        }
    }

    fun onExportButtonClicked() {
        dialogState = getExportDestinationOptionsDialog(
            onExportToFileOptionSelected = {
                emitEvent(ImportExportEvent.OpenFilePickerForExport(getExportFormat()))
            },
            onExportViaSharingOptionSelected = {
                sendExport()
            },
        )
    }

    fun onFilePickedForExport(file: Uri) {
        exporter.exportToUri(file, format = getExportFormat(), excludeDefaults = true)
            .doOnSubscribe {
                showProgressDialog(R.string.export_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                { status ->
                    showSnackbar(
                        QuantityStringLocalizable(
                            R.plurals.shortcut_export_success,
                            status.exportedShortcuts,
                            status.exportedShortcuts,
                        )
                    )
                },
                { error ->
                    logException(error)
                    dialogState = DialogState.create {
                        message(StringResLocalizable(R.string.export_failed_with_reason, error.message ?: error.javaClass.simpleName))
                            .positive(R.string.dialog_ok)
                            .build()
                    }
                },
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun sendExport() {
        val format = getExportFormat()
        val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single = false))

        exporter
            .exportToUri(cacheFile, excludeDefaults = true)
            .doOnSubscribe {
                showProgressDialog(R.string.export_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                {
                    openActivity(object : IntentBuilder {
                        override fun build(context: Context) =
                            Intent(Intent.ACTION_SEND)
                                .setType(format.fileTypeForSharing)
                                .putExtra(Intent.EXTRA_STREAM, cacheFile)
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                .let {
                                    Intent.createChooser(it, context.getString(R.string.title_export))
                                }
                    })
                },
                ::handleUnexpectedError,
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun showProgressDialog(message: Int) {
        dialogState = ProgressDialogState(StringResLocalizable(message), ::onProgressDialogCanceled)
    }

    private fun hideProgressDialog() {
        if (dialogState?.id == ProgressDialogState.DIALOG_ID) {
            dialogState = null
        }
    }

    private fun onProgressDialogCanceled() {
        disposable?.dispose()
    }

    private fun getExportFormat() =
        if (settings.useLegacyExportFormat) ExportFormat.LEGACY_JSON else ExportFormat.ZIP

    fun onRemoteEditorClosed(changesImported: Boolean) {
        if (changesImported) {
            setCategoriesChangedFlag()
        }
    }

    private fun setCategoriesChangedFlag() {
        setResult(
            intent = ImportExportActivity.OpenImportExport.createResult(categoriesChanged = true)
        )
    }

    fun onFilePickedForImport(file: Uri) {
        startImport(file)
    }

    private fun startImportFromURL(url: Uri) {
        persistImportUrl(url)
        if (url.isWebUrl) {
            startImport(url)
        } else {
            onImportFailedDueToInvalidUrl()
        }
    }

    private fun persistImportUrl(url: Uri) {
        settings.importUrl = url
    }

    private fun onImportFailedDueToInvalidUrl() {
        onImportFailed(StringResLocalizable(R.string.error_can_only_import_from_http_url))
    }

    private fun startImport(uri: Uri) {
        importer
            .importFromUri(uri, importMode = Importer.ImportMode.MERGE)
            .doOnSubscribe {
                showProgressDialog(R.string.import_in_progress)
            }
            .doFinally {
                hideProgressDialog()
            }
            .subscribe(
                { status ->
                    if (status.needsRussianWarning) {
                        dialogState = getRussianWarningDialog()
                    }
                    showSnackbar(
                        QuantityStringLocalizable(
                            R.plurals.shortcut_import_success,
                            status.importedShortcuts,
                            status.importedShortcuts,
                        )
                    )
                    setResult(
                        intent = ImportExportActivity.OpenImportExport.createResult(categoriesChanged = true)
                    )
                    setCategoriesChangedFlag()
                },
                { e ->
                    if (e !is ImportException) {
                        logException(e)
                    }
                    onImportFailed(StaticLocalizable(e.message ?: e::class.java.simpleName))
                },
            )
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun onImportFailed(message: Localizable) {
        dialogState = DialogState.create {
            message(StringResLocalizable(R.string.import_failed_with_reason, message))
                .positive(R.string.dialog_ok)
                .build()
        }
    }
}
