package ch.rmy.android.http_shortcuts.activities.importexport

import android.app.Application
import android.net.Uri
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.import_export.Exporter
import ch.rmy.android.http_shortcuts.import_export.ImportMode
import ch.rmy.android.http_shortcuts.import_export.ImportPasswordException
import ch.rmy.android.http_shortcuts.import_export.Importer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ImportExportViewModel
@Inject
constructor(
    application: Application,
    private val importer: Importer,
    private val exporter: Exporter,
) : BaseViewModel<Unit, ImportExportViewState>(application) {

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun initialize(data: Unit): ImportExportViewState = ImportExportViewState()

    fun onImportFromFileButtonClicked() = runAction {
        emitEvent(ImportExportEvent.OpenFilePickerForImport)
    }

    fun onFilePickedForImport(file: Uri) = runAction {
        startImport(file)
    }

    private fun ViewModelScope<*>.startImport(uri: Uri, password: String? = null, onComplete: suspend () -> Unit = {}) {
        currentJob?.cancel()
        currentJob = launch {
            try {
                showProgressDialog(R.string.import_in_progress)
                val status = importer.importFromUri(uri, importMode = ImportMode.MERGE, password)
                showToast(
                    QuantityStringLocalizable(
                        R.plurals.shortcut_import_success,
                        status.importedShortcuts,
                        status.importedShortcuts,
                    ),
                )
                runAction {
                    updateViewState {
                        copy(importStatus = status)
                    }
                }
                onComplete()
            } catch (e: CancellationException) {
                throw e
            } catch (_: ImportPasswordException) {
                if (password != null) {
                    delay(Random.nextInt(from = 100, until = 1000).milliseconds)
                }
                setDialogState(ImportExportDialogState.ImportPasswordPrompt(uri, tryAgain = password != null))
            } catch (e: Exception) {
                showError(StringResLocalizable(R.string.import_failed_with_reason, e.message ?: e::class.java.simpleName))
            } finally {
                hideProgressDialog()
            }
        }
    }

    fun onImportPasswordSubmitted(password: String) = runAction {
        val url = (viewState.dialogState as? ImportExportDialogState.ImportPasswordPrompt)?.url ?: skipAction()
        hideDialog()
        startImport(url, password)
    }

    fun onFilePickedForExport(file: Uri) = runAction {
        currentJob?.cancel()
        currentJob = launch {
            try {
                showProgressDialog(R.string.export_in_progress)
                exporter.exportToUri(
                    file,
                    excludeDefaults = true,
                )
                showToast(StringResLocalizable(R.string.export_success))
            } catch (e: Exception) {
                showError((e.message ?: e::class.java.simpleName ?: "Failed to export").toLocalizable())
            } finally {
                hideProgressDialog()
            }
        }
    }

    fun onExternalFileReceived(file: Uri) = runAction {
        startImport(file) {
            onExternalFileProcessed()
        }
    }

    private suspend fun onExternalFileProcessed() {
        val exportFile = File(context.cacheDir, "export.zip")
        val exportUri = FileUtil.getUriFromFile(context, exportFile)
        try {
            showProgressDialog(R.string.export_in_progress)
            exporter.exportToUri(
                exportUri,
                excludeDefaults = true,
            )
            emitEvent(ImportExportEvent.SendExport(exportUri))
        } catch (e: Exception) {
            showError((e.message ?: e::class.java.simpleName ?: "Failed to export").toLocalizable())
        } finally {
            hideProgressDialog()
        }
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
        closeScreen()
    }
}
