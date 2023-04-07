package ch.rmy.android.http_shortcuts.activities.settings.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StaticLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.importexport.usecases.GetShortcutSelectionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class ImportExportViewModel(application: Application) :
    BaseViewModel<ImportExportViewModel.InitData, ImportExportViewState>(application) {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var getShortcutSelectionDialog: GetShortcutSelectionDialogUseCase

    @Inject
    lateinit var exporter: Exporter

    @Inject
    lateinit var importer: Importer

    @Inject
    lateinit var getUsedVariableIds: GetUsedVariableIdsUseCase

    private var shortcutIdsForExport: Collection<ShortcutId>? = null

    init {
        getApplicationComponent().inject(this)
    }

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override fun initViewState() = ImportExportViewState(
        useLegacyFormat = settings.useLegacyExportFormat,
    )

    override fun onInitialized() {
        if (initData.importUrl != null) {
            openImportUrlDialog(initData.importUrl!!.toString())
        }
    }

    fun onImportFromFileButtonClicked() {
        emitEvent(ImportExportEvent.OpenFilePickerForImport)
    }

    fun onImportFromURLButtonClicked() {
        openImportUrlDialog(prefill = settings.importUrl?.toString() ?: "")
    }

    private fun openImportUrlDialog(prefill: String) {
        setDialogState(ImportExportDialogState.ImportFromUrl(initialValue = prefill))
    }

    fun onExportButtonClicked() {
        viewModelScope.launch {
            setDialogState(getShortcutSelectionDialog())
        }
    }

    fun onShortcutsForExportSelected(shortcutIds: Collection<ShortcutId>?) {
        shortcutIdsForExport = shortcutIds
        setDialogState(
            ImportExportDialogState.SelectExportDestinationDialog,
        )
    }

    fun onExportToFileOptionSelected() {
        hideDialog()
        emitEvent(ImportExportEvent.OpenFilePickerForExport(getExportFormat()))
    }

    fun onFilePickedForExport(file: Uri) {
        hideDialog()
        currentJob = viewModelScope.launch {
            startExportToUri(shortcutIdsForExport, file)
            shortcutIdsForExport = null
        }
    }

    fun onExportViaSharingOptionSelected() {
        hideDialog()
        currentJob = viewModelScope.launch {
            sendExport(shortcutIdsForExport)
            shortcutIdsForExport = null
        }
    }

    private suspend fun getVariableIdsForExport(shortcutIds: Collection<ShortcutId>?): Set<VariableId>? =
        if (shortcutIds != null) {
            getUsedVariableIds(shortcutIds)
        } else null

    private suspend fun startExportToUri(shortcutIds: Collection<ShortcutId>?, file: Uri) {
        try {
            showProgressDialog(R.string.export_in_progress)
            val variableIds = getVariableIdsForExport(shortcutIds)
            val status = exporter.exportToUri(
                file,
                shortcutIds = shortcutIds,
                variableIds = variableIds,
                format = getExportFormat(),
                excludeDefaults = true,
            )

            showSnackbar(
                QuantityStringLocalizable(
                    R.plurals.shortcut_export_success,
                    status.exportedShortcuts,
                    status.exportedShortcuts,
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
            showError(StringResLocalizable(R.string.export_failed_with_reason, e.message ?: e.javaClass.simpleName))
        } finally {
            hideProgressDialog()
        }
    }

    private suspend fun sendExport(shortcutIds: Collection<ShortcutId>?) {
        val format = getExportFormat()
        val cacheFile = FileUtil.createCacheFile(context, format.getFileName(single = false))

        try {
            showProgressDialog(R.string.export_in_progress)
            val variableIds = getVariableIdsForExport(shortcutIds)
            exporter
                .exportToUri(
                    cacheFile,
                    shortcutIds = shortcutIds,
                    variableIds = variableIds,
                    excludeDefaults = true,
                )

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
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleUnexpectedError(e)
        } finally {
            hideProgressDialog()
        }
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

    fun onImportFromUrlDialogSubmitted(urlValue: String) {
        val url = urlValue.toUri()
        hideDialog()
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
        showError(StringResLocalizable(R.string.error_can_only_import_from_http_url))
    }

    private fun startImport(uri: Uri) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                showProgressDialog(R.string.import_in_progress)
                val status = importer.importFromUri(uri, importMode = Importer.ImportMode.MERGE)
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (e !is ImportException) {
                    logException(e)
                }
                showError(StaticLocalizable(e.message ?: e::class.java.simpleName))
            } finally {
                hideProgressDialog()
            }
        }
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.IMPORT_EXPORT_DOCUMENTATION)
    }

    fun onLegacyFormatUseChanged(useLegacyFormat: Boolean) {
        settings.useLegacyExportFormat = useLegacyFormat
    }

    fun onDialogDismissalRequested() {
        currentJob?.cancel()
        hideDialog()
    }

    private fun setDialogState(dialogState: ImportExportDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    private fun showProgressDialog(message: Int) {
        setDialogState(ImportExportDialogState.Progress(StringResLocalizable(message)))
    }

    private fun hideProgressDialog() {
        doWithViewState {
            if (it.dialogState is ImportExportDialogState.Progress) {
                hideDialog()
            }
        }
    }

    private fun showError(message: Localizable) {
        setDialogState(ImportExportDialogState.Error(message))
    }

    private fun hideDialog() {
        setDialogState(null)
    }

    data class InitData(
        val importUrl: Uri?,
    )
}
