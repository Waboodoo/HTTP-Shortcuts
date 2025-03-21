package ch.rmy.android.http_shortcuts.activities.workingdirectories

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.workingdirectories.models.WorkingDirectoryListItem
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class WorkingDirectoriesViewModel
@Inject
constructor(
    application: Application,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
) : BaseViewModel<WorkingDirectoriesViewModel.InitData, WorkingDirectoriesViewState>(application) {

    private lateinit var workingDirectories: List<WorkingDirectory>

    private var workingDirectoryIdForPicker: WorkingDirectoryId? = null

    override suspend fun initialize(data: InitData): WorkingDirectoriesViewState {
        val workingDirectoriesFlow = workingDirectoryRepository.observeWorkingDirectories()
        workingDirectories = workingDirectoriesFlow.first()

        viewModelScope.launch(Dispatchers.IO) {
            workingDirectoriesFlow.collectLatest { directories ->
                workingDirectories = directories
                updateViewState {
                    copy(workingDirectories = directories.map { it.toListItem() })
                }
            }
        }
        if (workingDirectories.isEmpty() && initData.picker) {
            emitEvent(WorkingDirectoriesEvent.OpenDirectoryPicker())
        }
        return WorkingDirectoriesViewState(
            workingDirectories = workingDirectories.map {
                it.toListItem()
            },
        )
    }

    private fun WorkingDirectory.toListItem() =
        WorkingDirectoryListItem(
            id = id,
            name = name,
            lastAccessed = accessed?.let {
                LocalDateTime.ofInstant(it, ZoneId.systemDefault())
            },
            unmounted = try {
                DocumentFile.fromTreeUri(context, directory)?.isDirectory != true
            } catch (_: IllegalArgumentException) {
                true
            },
        )

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.WORKING_DIRECTORIES_DOCUMENTATION)
    }

    fun onCreateButtonClicked() = runAction {
        workingDirectoryIdForPicker = null
        emitEvent(WorkingDirectoriesEvent.OpenDirectoryPicker())
    }

    fun onWorkingDirectoryClicked(workingDirectoryId: WorkingDirectoryId) = runAction {
        val workingDirectory = findWorkingDirectory(workingDirectoryId)
            ?: return@runAction
        if (initData.picker) {
            selectDirectory(workingDirectoryId, name = workingDirectory.name)
        } else {
            updateDialogState(
                WorkingDirectoriesDialogState.ContextMenu(
                    title = workingDirectory.name,
                    workingDirectoryId = workingDirectoryId,
                ),
            )
        }
    }

    private fun findWorkingDirectory(workingDirectoryId: WorkingDirectoryId): WorkingDirectory? =
        workingDirectories.find { it.id == workingDirectoryId }

    private fun selectDirectory(workingDirectoryId: WorkingDirectoryId, name: String) = runAction {
        closeScreen(
            result = NavigationDestination.WorkingDirectories.WorkingDirectoryPickerResult(
                workingDirectoryId = workingDirectoryId,
                name = name,
            ),
        )
    }

    fun onDirectoryPicked(directoryUri: Uri) = runAction {
        val workingDirectoryId = workingDirectoryIdForPicker
        workingDirectoryIdForPicker = null
        withProgressTracking {
            if (workingDirectoryId != null) {
                findWorkingDirectory(workingDirectoryId)?.let { workingDirectory ->
                    if (workingDirectory.directory != directoryUri) {
                        revokeAccess(workingDirectory.directory)
                    }
                }
                workingDirectoryRepository.setDirectoryUri(workingDirectoryId, directoryUri)
                showSnackbar(R.string.message_working_directory_mounted)
            } else {
                val workingDirectory = workingDirectoryRepository.createWorkingDirectory(
                    name = directoryUri.getStoreDirectoryName() ?: "dir",
                    directoryUri,
                )
                if (initData.picker) {
                    selectDirectory(workingDirectory.id, workingDirectory.name)
                } else {
                    showSnackbar(R.string.message_working_directory_mounted)
                }
            }

            updateViewState {
                copy(workingDirectories = this@WorkingDirectoriesViewModel.workingDirectories.map { it.toListItem() })
            }
        }
    }

    private fun Uri.getStoreDirectoryName(): String? =
        DocumentFile.fromTreeUri(context, this)?.name

    fun onRenameClicked() = runAction {
        val workingDirectoryId = viewState.getWorkingDirectoryIdFromContextMenu() ?: return@runAction
        val workingDirectory = findWorkingDirectory(workingDirectoryId) ?: return@runAction
        updateDialogState(
            WorkingDirectoriesDialogState.Rename(
                workingDirectoryId = workingDirectoryId,
                oldName = workingDirectory.name,
                existingNames = workingDirectories.map { it.name }.minus(workingDirectory.name).toSet(),
            ),
        )
    }

    fun onRenameConfirmed(newName: String) = runAction {
        val workingDirectoryId = (viewState.dialogState as? WorkingDirectoriesDialogState.Rename)?.workingDirectoryId ?: return@runAction
        updateDialogState(null)
        withProgressTracking {
            workingDirectoryRepository.renameWorkingDirectory(workingDirectoryId, newName)
        }
    }

    private fun WorkingDirectoriesViewState.getWorkingDirectoryIdFromContextMenu(): WorkingDirectoryId? =
        (dialogState as? WorkingDirectoriesDialogState.ContextMenu)?.workingDirectoryId

    fun onMountClicked() = runAction {
        val workingDirectoryId = viewState.getWorkingDirectoryIdFromContextMenu() ?: return@runAction
        workingDirectoryIdForPicker = workingDirectoryId
        updateDialogState(null)
        emitEvent(WorkingDirectoriesEvent.OpenDirectoryPicker(initialDirectory = findWorkingDirectory(workingDirectoryId)?.directory))
    }

    fun onDeleteClicked() = runAction {
        val workingDirectoryId = viewState.getWorkingDirectoryIdFromContextMenu() ?: return@runAction
        val workingDirectory = findWorkingDirectory(workingDirectoryId) ?: return@runAction
        updateDialogState(
            WorkingDirectoriesDialogState.Delete(
                title = workingDirectory.name,
                workingDirectoryId = workingDirectoryId,
            ),
        )
    }

    fun onDeleteConfirmed() = runAction {
        val workingDirectoryId = (viewState.dialogState as? WorkingDirectoriesDialogState.Delete)?.workingDirectoryId ?: return@runAction
        updateDialogState(null)
        withProgressTracking {
            findWorkingDirectory(workingDirectoryId)?.let { workingDirectory ->
                revokeAccess(workingDirectory.directory)
            }
            workingDirectoryRepository.deleteWorkingDirectory(workingDirectoryId)
        }
        findWorkingDirectory(workingDirectoryId)
            ?.let { workingDirectory ->
                showSnackbar(StringResLocalizable(R.string.working_directory_deleted, workingDirectory.name))
            }
    }

    private fun revokeAccess(directoryUri: Uri) {
        tryOrIgnore {
            if (context.contentResolver.persistedUriPermissions.isNotEmpty()) {
                context.contentResolver.releasePersistableUriPermission(
                    directoryUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
    }

    fun onDialogDismissalRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: WorkingDirectoriesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    data class InitData(
        val picker: Boolean,
    )
}
