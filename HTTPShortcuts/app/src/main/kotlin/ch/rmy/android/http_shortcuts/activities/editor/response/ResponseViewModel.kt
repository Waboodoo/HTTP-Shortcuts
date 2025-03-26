package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.framework.extensions.toCharset
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.nio.charset.Charset
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class ResponseViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val workingDirectoryRepository: WorkingDirectoryRepository,
) : BaseViewModel<Unit, ResponseViewState>(application) {

    override suspend fun initialize(data: Unit): ResponseViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        val storeDirectoryName = try {
            shortcut.responseStoreDirectoryId
                ?.let { id ->
                    workingDirectoryRepository.getWorkingDirectoryById(id)
                }
                ?.name
        } catch (_: NoSuchElementException) {
            "???"
        }

        runAction {
            val charsets = withContext(Dispatchers.Default) {
                Charset.availableCharsets().values.toList()
            }
            updateViewState {
                copy(availableCharsets = charsets.map { it.name() })
            }
        }

        return ResponseViewState(
            successMessageHint = getSuccessMessageHint(shortcut),
            responseUiType = shortcut.responseUiType,
            responseSuccessOutput = shortcut.responseSuccessOutput,
            responseFailureOutput = shortcut.responseFailureOutput,
            successMessage = shortcut.responseSuccessMessage,
            responseCharset = shortcut.responseCharset?.name(),
            availableCharsets = emptyList(),
            storeResponseIntoFile = shortcut.responseStoreDirectoryId != null,
            storeDirectoryName = storeDirectoryName,
            storeFileName = shortcut.responseStoreFileName.orEmpty(),
            replaceFileIfExists = shortcut.responseReplaceFileIfExists,
        )
    }

    private fun getSuccessMessageHint(shortcut: Shortcut): Localizable =
        StringResLocalizable(
            R.string.executed,
            Localizable.create { context ->
                shortcut.name.ifEmpty { context.getString(R.string.shortcut_safe_name) }
            },
        )

    fun onResponseUiTypeChanged(responseUiType: ResponseUiType) = runAction {
        updateViewState {
            copy(responseUiType = responseUiType)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseUiType(responseUiType)
        }
    }

    fun onResponseSuccessOutputChanged(responseSuccessOutput: ResponseSuccessOutput) = runAction {
        updateViewState {
            copy(responseSuccessOutput = responseSuccessOutput)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseSuccessOutput(responseSuccessOutput)
        }
    }

    fun onResponseFailureOutputChanged(responseFailureOutput: ResponseFailureOutput) = runAction {
        updateViewState {
            copy(responseFailureOutput = responseFailureOutput)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseFailureOutput(responseFailureOutput)
        }
    }

    fun onSuccessMessageChanged(successMessage: String) = runAction {
        updateViewState {
            copy(successMessage = successMessage)
        }
        withProgressTracking {
            temporaryShortcutRepository.setResponseSuccessMessage(successMessage)
        }
    }

    fun onDisplaySettingsClicked() = runAction {
        navigate(NavigationDestination.ShortcutEditorResponseDisplay)
    }

    fun onResponseCharsetChanged(charset: String?) = runAction {
        updateViewState {
            copy(responseCharset = charset)
        }
        withProgressTracking {
            temporaryShortcutRepository.setCharsetOverride(
                charset?.toCharset(),
            )
        }
    }

    fun onStoreIntoFileCheckboxChanged(enabled: Boolean) = runAction {
        if (enabled == viewState.storeResponseIntoFile) {
            skipAction()
        }
        if (enabled) {
            navigate(NavigationDestination.WorkingDirectories.buildRequest(picker = true))
        } else {
            updateViewState {
                copy(
                    storeResponseIntoFile = false,
                    storeDirectoryName = null,
                )
            }
            withProgressTracking {
                temporaryShortcutRepository.setStoreDirectory(null)
            }
        }
    }

    fun onStoreFileNameChanged(storeFileName: String) = runAction {
        updateViewState {
            copy(storeFileName = storeFileName)
        }
        withProgressTracking {
            temporaryShortcutRepository.setStoreFileName(storeFileName)
        }
    }

    fun onWorkingDirectoryPicked(workingDirectoryId: String, name: String) = runAction {
        updateViewState {
            copy(
                storeResponseIntoFile = true,
                storeDirectoryName = name,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setStoreDirectory(workingDirectoryId)
        }
    }

    fun onStoreFileOverwriteChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(replaceFileIfExists = enabled)
        }
        withProgressTracking {
            temporaryShortcutRepository.setStoreReplaceIfExists(enabled)
        }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }
}
