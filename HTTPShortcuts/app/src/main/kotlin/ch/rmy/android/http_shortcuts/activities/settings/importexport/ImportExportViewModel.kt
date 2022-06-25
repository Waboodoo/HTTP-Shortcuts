package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.Optional
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
import ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases.GetShortcutSelectionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.usecases.GetExportDestinationOptionsDialogUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.FileUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ImportExportViewModel(application: Application) :
    BaseViewModel<ImportExportViewModel.InitData, ImportExportViewState>(application),
    WithDialog {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var getExportDestinationOptionsDialog: GetExportDestinationOptionsDialogUseCase

    @Inject
    lateinit var getShortcutSelectionDialog: GetShortcutSelectionDialogUseCase

    @Inject
    lateinit var exporter: Exporter

    @Inject
    lateinit var importer: Importer

    @Inject
    lateinit var getRussianWarningDialog: GetRussianWarningDialogUseCase

    @Inject
    lateinit var getUsedVariableIds: GetUsedVariableIdsUseCase

    private var shortcutIdsForExport: Collection<ShortcutId>? = null

    init {
        getApplicationComponent().inject(this)
    }

    private var disposable: Disposable? = null

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = ImportExportViewState()

    override fun onInitialized() {
        if (initData.importUrl != null) {
            openImportUrlDialog(initData.importUrl!!.toString())
        }
    }

    fun onImportFromURLButtonClicked() {
        openImportUrlDialog(prefill = settings.importUrl?.toString() ?: "")
    }

    private fun openImportUrlDialog(prefill: String) {
        dialogState = DialogState.create {
            title(R.string.dialog_title_import_from_url)
                .textInput(
                    prefill = prefill,
                    allowEmpty = false,
                    callback = { startImportFromURL(it.toUri()) },
                )
                .build()
        }
    }

    fun onExportButtonClicked() {
        getShortcutSelectionDialog(::onShortcutsForExportSelected)
            .withProgressDialog(R.string.export_in_progress)
            .subscribe { dialogState ->
                this.dialogState = dialogState
            }
            .also {
                disposable = it
            }
            .attachTo(destroyer)
    }

    private fun onShortcutsForExportSelected(shortcutIds: Collection<ShortcutId>?) {
        shortcutIdsForExport = shortcutIds
        dialogState = getExportDestinationOptionsDialog(
            onExportToFileOptionSelected = {
                emitEvent(ImportExportEvent.OpenFilePickerForExport(getExportFormat()))
            },
            onExportViaSharingOptionSelected = ::onExportViaSharingOptionSelected,
        )
    }

    fun onFilePickedForExport(file: Uri) {
        startExportToUri(shortcutIdsForExport, file)
        shortcutIdsForExport = null
    }

    private fun onExportViaSharingOptionSelected() {
        sendExport(shortcutIdsForExport)
        shortcutIdsForExport = null
    }

    private fun getVariableIdsForExport(shortcutIds: Collection<ShortcutId>?): Single<Optional<Set<VariableId>>> =
        if (shortcutIds != null) {
            getUsedVariableIds(shortcutIds)
                .map(::Optional)
        } else {
            Single.just(Optional.empty())
        }

    private fun startExportToUri(shortcutIds: Collection<ShortcutId>?, file: Uri) {
        getVariableIdsForExport(shortcutIds)
            .flatMap { variableIds ->
                exporter.exportToUri(
                    file,
                    shortcutIds = shortcutIds,
                    variableIds = variableIds.value,
                    format = getExportFormat(),
                    excludeDefaults = true,
                )
            }
            .withProgressDialog(R.string.export_in_progress)
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

    private fun sendExport(shortcutIds: Collection<ShortcutId>?) {
        val format = getExportFormat()
        val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single = false))

        getVariableIdsForExport(shortcutIds)
            .flatMap { variableIds ->
                exporter
                    .exportToUri(
                        cacheFile,
                        shortcutIds = shortcutIds,
                        variableIds = variableIds.value,
                        excludeDefaults = true,
                    )
            }
            .withProgressDialog(R.string.export_in_progress)
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

    private fun <T> Single<T>.withProgressDialog(@StringRes label: Int): Single<T> =
        doOnSubscribe {
            showProgressDialog(label)
        }
            .doFinally {
                hideProgressDialog()
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
            .withProgressDialog(R.string.import_in_progress)
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

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.IMPORT_EXPORT_DOCUMENTATION)
    }

    data class InitData(
        val importUrl: Uri?,
    )
}
