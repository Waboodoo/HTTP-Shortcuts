package ch.rmy.android.http_shortcuts.activities.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.importexport.usecases.GetShortcutSelectionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val shortcutRepository: ShortcutRepository,
    private val getShortcutSelectionDialog: GetShortcutSelectionDialogUseCase,
    private val exporter: Exporter,
    private val importer: Importer,
    private val getUsedVariableIds: GetUsedVariableIdsUseCase,
) : BaseViewModel<ImportExportViewModel.InitData, ImportExportViewState>(application) {

    private var shortcutIdsForExport: Collection<ShortcutId>? = null

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var hasShortcuts = false

    override suspend fun initialize(data: InitData): ImportExportViewState {
        hasShortcuts = shortcutRepository.getShortcuts().isNotEmpty()

        if (initData.importUrl != null) {
            viewModelScope.launch {
                openImportUrlDialog(initData.importUrl!!.toString())
            }
        }

        viewModelScope.launch {
            shortcutRepository.getObservableShortcuts().collect {
                updateViewState {
                    copy(exportEnabled = it.isNotEmpty())
                }
            }
        }

        return ImportExportViewState(
            exportEnabled = hasShortcuts,
        )
    }

    fun onImportFromFileButtonClicked() = runAction {
        emitEvent(ImportExportEvent.OpenFilePickerForImport)
    }

    fun onImportFromURLButtonClicked() = runAction {
        openImportUrlDialog(prefill = settings.importUrl?.toString() ?: "")
    }

    private suspend fun openImportUrlDialog(prefill: String) {
        setDialogState(ImportExportDialogState.ImportFromUrl(initialValue = prefill))
    }

    fun onExportButtonClicked() = runAction {
        setDialogState(getShortcutSelectionDialog())
    }

    fun onShortcutsForExportSelected(shortcutIds: Collection<ShortcutId>?) = runAction {
        shortcutIdsForExport = shortcutIds
        setDialogState(
            ImportExportDialogState.SelectExportDestinationDialog,
        )
    }

    fun onExportToFileOptionSelected() = runAction {
        hideDialog()
        emitEvent(ImportExportEvent.OpenFilePickerForExport)
    }

    fun onFilePickedForExport(file: Uri) = runAction {
        hideDialog()
        currentJob = launch {
            startExportToUri(shortcutIdsForExport, file)
            shortcutIdsForExport = null
        }
    }

    fun onExportViaSharingOptionSelected() = runAction {
        hideDialog()
        currentJob = launch {
            sendExport(shortcutIdsForExport)
            shortcutIdsForExport = null
        }
    }

    private suspend fun getVariableIdsForExport(shortcutIds: Collection<ShortcutId>?): Set<VariableId>? =
        if (shortcutIds != null) {
            getUsedVariableIds(shortcutIds)
        } else null

    private suspend fun ViewModelScope<*>.startExportToUri(shortcutIds: Collection<ShortcutId>?, file: Uri) {
        try {
            showProgressDialog(R.string.export_in_progress)
            val variableIds = getVariableIdsForExport(shortcutIds)
            val status = exporter.exportToUri(
                file,
                shortcutIds = shortcutIds,
                variableIds = variableIds,
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
        val cacheFile = FileUtil.createCacheFile(context, ExportFormat.ZIP.getFileName(single = false))

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
                        .setType(ExportFormat.ZIP.fileTypeForSharing)
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

    fun onRemoteEditorClosed(changesImported: Boolean) = runAction {
        if (changesImported) {
            setCategoriesChangedFlag()
        }
    }

    private suspend fun setCategoriesChangedFlag() {
        setResult(
            intent = ImportExportActivity.OpenImportExport.createResult(categoriesChanged = true)
        )
    }

    fun onFilePickedForImport(file: Uri) = runAction {
        logInfo("Starting import from file $file")
        startImport(file)
    }

    fun onImportFromUrlDialogSubmitted(urlValue: String) = runAction {
        val url = urlValue.toUri()
        hideDialog()
        persistImportUrl(url)
        if (url.isWebUrl) {
            logInfo("Starting info from external URL")
            startImport(url)
        } else {
            onImportFailedDueToInvalidUrl()
        }
    }

    private fun persistImportUrl(url: Uri) {
        settings.importUrl = url
    }

    private suspend fun onImportFailedDueToInvalidUrl() {
        showError(StringResLocalizable(R.string.error_can_only_import_from_http_url))
    }

    private fun ViewModelScope<*>.startImport(uri: Uri) {
        currentJob?.cancel()
        currentJob = launch {
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
                showError(StringResLocalizable(R.string.import_failed_with_reason, e.message ?: e::class.java.simpleName))
            } finally {
                hideProgressDialog()
            }
        }
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.IMPORT_EXPORT_DOCUMENTATION)
    }

    fun onDialogDismissalRequested() = runAction {
        currentJob?.cancel()
        hideDialog()
    }

    private suspend fun setDialogState(dialogState: ImportExportDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    private suspend fun showProgressDialog(message: Int) {
        setDialogState(ImportExportDialogState.Progress(StringResLocalizable(message)))
    }

    private suspend fun hideProgressDialog() {
        if (getCurrentViewState().dialogState is ImportExportDialogState.Progress) {
            hideDialog()
        }
    }

    private suspend fun showError(message: Localizable) {
        setDialogState(ImportExportDialogState.Error(message))
    }

    private suspend fun hideDialog() {
        setDialogState(null)
    }

    data class InitData(
        val importUrl: Uri?,
    )
}
