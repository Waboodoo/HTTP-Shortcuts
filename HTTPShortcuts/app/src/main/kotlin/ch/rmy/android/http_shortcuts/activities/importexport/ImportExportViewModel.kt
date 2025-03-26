package ch.rmy.android.http_shortcuts.activities.importexport

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.isWebUrl
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.import_export.ImportException
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.Importer
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.ImportExport.RESULT_CATEGORIES_CHANGED_FROM_IMPORT
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class ImportExportViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val shortcutRepository: ShortcutRepository,
    private val importer: Importer,
) : BaseViewModel<ImportExportViewModel.InitData, ImportExportViewState>(application) {

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    private var hasShortcuts = false
    private var categoriesChanged = false

    override suspend fun initialize(data: InitData): ImportExportViewState {
        hasShortcuts = shortcutRepository.getShortcuts().isNotEmpty()

        if (initData.importUrl != null) {
            viewModelScope.launch {
                openImportUrlDialog(initData.importUrl!!.toString())
            }
        }

        viewModelScope.launch {
            shortcutRepository.observeShortcuts().collect {
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

    fun onExportToFileButtonClicked() = runAction {
        navigate(NavigationDestination.Export.buildRequest(toFile = true))
    }

    fun onExportViaShareButtonClicked() = runAction {
        navigate(NavigationDestination.Export.buildRequest(toFile = false))
    }

    fun onRemoteEditorChangesImported() = runAction {
        categoriesChanged = true
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
                val status = importer.importFromUri(uri, importMode = ImportMode.MERGE)

                showSnackbar(
                    QuantityStringLocalizable(
                        R.plurals.shortcut_import_success,
                        status.importedShortcuts,
                        status.importedShortcuts,
                    ),
                )
                categoriesChanged = true
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

    fun onBackPressed() = runAction {
        closeScreen(result = if (categoriesChanged) RESULT_CATEGORIES_CHANGED_FROM_IMPORT else null)
    }

    fun onRemoteEditButtonClicked() = runAction {
        navigate(NavigationDestination.RemoteEdit)
    }

    data class InitData(
        val importUrl: Uri?,
    )
}
