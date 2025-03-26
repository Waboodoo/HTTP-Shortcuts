package ch.rmy.android.http_shortcuts.activities.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.ui.IntentBuilder
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.importexport.models.ExportItem
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.import_export.ExportFormat
import ch.rmy.android.http_shortcuts.import_export.Exporter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@HiltViewModel
class ExportViewModel
@Inject
constructor(
    application: Application,
    private val categoryRepository: CategoryRepository,
    private val shortcutRepository: ShortcutRepository,
    private val getUsedVariableIds: GetUsedVariableIdsUseCase,
    private val exporter: Exporter,
) : BaseViewModel<ExportViewModel.InitData, ExportViewState>(application) {

    private var currentJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override suspend fun initialize(data: InitData): ExportViewState {
        val items = buildList {
            val shortcutsByCategoryId = shortcutRepository.getShortcuts().groupBy { it.categoryId }
            categoryRepository.getCategories().forEach { category ->
                val shortcuts = shortcutsByCategoryId[category.id] ?: emptyList()
                if (shortcuts.isNotEmpty()) {
                    add(
                        ExportItem.Category(
                            categoryId = category.id,
                            name = category.name,
                            checked = true,
                        ),
                    )
                    shortcuts.forEach { shortcut ->
                        add(
                            ExportItem.Shortcut(
                                shortcutId = shortcut.id,
                                categoryId = category.id,
                                name = shortcut.name,
                                icon = shortcut.icon,
                                checked = true,
                            ),
                        )
                    }
                }
            }
        }

        return ExportViewState(
            items = items,
        )
    }

    private fun ExportViewState.getSelectedShortcutIds(): List<ShortcutId> =
        items.filterIsInstance<ExportItem.Shortcut>()
            .filter { it.checked }
            .map { it.shortcutId }

    fun onExportButtonClicked() = runAction {
        if (initData.toFile) {
            emitEvent(ExportEvent.OpenFilePickerForExport)
        } else {
            currentJob = launch {
                sendExport()
            }
        }
    }

    fun onFilePickedForExport(file: Uri) = runAction {
        hideDialog()
        currentJob = launch {
            startExportToUri(file)
        }
    }

    private suspend fun ViewModelScope<ExportViewState>.startExportToUri(file: Uri) {
        val shortcutIds = viewState.getSelectedShortcutIds()
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
                ),
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

    private suspend fun ViewModelScope<ExportViewState>.sendExport() {
        val shortcutIds = viewState.getSelectedShortcutIds()
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

            sendIntent(
                object : IntentBuilder {
                    override fun build(context: Context) =
                        Intent(Intent.ACTION_SEND)
                            .setType(ExportFormat.ZIP.fileTypeForSharing)
                            .putExtra(Intent.EXTRA_STREAM, cacheFile)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            .let {
                                Intent.createChooser(it, context.getString(R.string.title_export))
                            }
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleUnexpectedError(e)
        } finally {
            hideProgressDialog()
        }
    }

    private suspend fun getVariableIdsForExport(shortcutIds: Collection<ShortcutId>?): Set<VariableId>? =
        if (shortcutIds != null) {
            getUsedVariableIds(shortcutIds)
        } else {
            null
        }

    fun onDialogDismissalRequested() = runAction {
        currentJob?.cancel()
        hideDialog()
    }

    private suspend fun showError(message: Localizable) {
        setDialogState(ExportDialogState.Error(message))
    }

    private suspend fun showProgressDialog(message: Int) {
        setDialogState(ExportDialogState.Progress(StringResLocalizable(message)))
    }

    private suspend fun hideProgressDialog() {
        if (getCurrentViewState().dialogState is ExportDialogState.Progress) {
            hideDialog()
        }
    }

    private suspend fun hideDialog() {
        setDialogState(null)
    }

    private suspend fun setDialogState(dialogState: ExportDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onShortcutCheckedChanged(shortcutId: ShortcutId, checked: Boolean) = runAction {
        updateViewState {
            var affectedCategoryId: CategoryId? = null
            val updatedItems = items.map { item ->
                if ((item as? ExportItem.Shortcut)?.shortcutId == shortcutId) {
                    affectedCategoryId = item.categoryId
                    item.copy(checked = checked)
                } else {
                    item
                }
            }
                .runIfNotNull(affectedCategoryId) { categoryId ->
                    val allChecked = all {
                        it !is ExportItem.Shortcut || it.categoryId != categoryId || it.checked
                    }
                    map { item ->
                        if ((item as? ExportItem.Category)?.categoryId == categoryId) {
                            item.copy(checked = allChecked)
                        } else {
                            item
                        }
                    }
                }
            copy(
                items = updatedItems,
            )
        }
    }

    fun onCategoryCheckedChanged(categoryId: CategoryId, checked: Boolean) = runAction {
        updateViewState {
            copy(
                items = items.map { item ->
                    when (item) {
                        is ExportItem.Category -> if (item.categoryId == categoryId) {
                            item.copy(checked = checked)
                        } else {
                            item
                        }
                        is ExportItem.Shortcut -> if (item.categoryId == categoryId) {
                            item.copy(checked = checked)
                        } else {
                            item
                        }
                    }
                },
            )
        }
    }

    fun onSelectAllButtonClicked() = runAction {
        updateViewState {
            copy(
                items = items.map { item ->
                    when (item) {
                        is ExportItem.Category -> item.copy(checked = true)
                        is ExportItem.Shortcut -> item.copy(checked = true)
                    }
                },
            )
        }
    }

    fun onDeselectAllButtonClicked() = runAction {
        updateViewState {
            copy(
                items = items.map { item ->
                    when (item) {
                        is ExportItem.Category -> item.copy(checked = false)
                        is ExportItem.Shortcut -> item.copy(checked = false)
                    }
                },
            )
        }
    }

    data class InitData(
        val toFile: Boolean,
    )
}
