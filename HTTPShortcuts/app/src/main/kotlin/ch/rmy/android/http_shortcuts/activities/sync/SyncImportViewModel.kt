package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import android.net.Uri
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.sync.SyncConfigMonitor
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.WorkingDirectoryUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SyncImportViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
    private val workingDirectoryUtil: WorkingDirectoryUtil,
    private val syncConfigMonitor: SyncConfigMonitor,
) : BaseViewModel<Unit, SyncImportViewState>(application) {
    private lateinit var configFlow: MutableStateFlow<SyncConfig>

    @OptIn(FlowPreview::class)
    override suspend fun initialize(data: Unit): SyncImportViewState {
        viewModelScope.launch {
            syncConfigMonitor.configurationInProgress()
        }

        if (userPreferences.syncType != SyncType.IMPORT) {
            terminateInitialization()
        }

        val config = syncRepository.getConfig(SyncType.IMPORT)
        configFlow = MutableStateFlow(config)
        viewModelScope.launch(Dispatchers.Default) {
            configFlow.drop(1)
                .debounce(300.milliseconds)
                .collectLatest { config ->
                    syncRepository.updateConfig(config)
                }
        }
        return SyncImportViewState(
            schedule = config.schedule,
            targetType = config.targetType,
            filePassword = config.filePassword,
            directoryName = config.targetDirectoryUri?.let { workingDirectoryUtil.getDocumentFile(it) }?.name ?: "",
            fileName = config.targetFileName ?: "",
            webUrl = config.targetUrl ?: "https://",
            webAuthUsername = config.targetAuthUsername ?: "",
            webAuthPassword = config.targetAuthPassword ?: "",
            replaceLocal = config.replaceLocal,
            hasChanged = false,
        )
    }

    private fun updateConfig(update: SyncConfig.() -> SyncConfig) {
        configFlow.update { update(it) }
    }

    fun onScheduleChanged(schedule: SyncSchedule) = runAction {
        updateViewState {
            copy(
                schedule = schedule,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(schedule = schedule)
        }
    }

    fun onFilePasswordChanged(password: String) = runAction {
        updateViewState {
            copy(
                filePassword = password,
                hasChanged = true,
            )
        }
        updateConfig { copy(filePassword = password) }
    }

    fun onTargetTypeChanged(targetType: SyncTargetType) = runAction {
        updateViewState {
            copy(
                targetType = targetType,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetType = targetType)
        }
    }

    fun onDirectoryPicked(directory: Uri) = runAction {
        updateViewState {
            copy(
                directoryName = workingDirectoryUtil.getDocumentFile(directory)?.name ?: "",
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetDirectoryUri = directory)
        }
    }

    fun onFileNameChanged(fileName: String) = runAction {
        val fileName = fileName.replace("/", "")
        updateViewState {
            copy(
                fileName = fileName,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetFileName = fileName.takeUnlessEmpty())
        }
    }

    fun onWebUrlChanged(url: String) = runAction {
        updateViewState {
            copy(
                webUrl = url,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetUrl = url.takeUnlessEmpty()?.takeUnless { it == "https://" })
        }
    }

    fun onWebAuthUsernameChanged(username: String) = runAction {
        updateViewState {
            copy(
                webAuthUsername = username,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetAuthUsername = username.takeUnlessEmpty())
        }
    }

    fun onWebAuthPasswordChanged(password: String) = runAction {
        updateViewState {
            copy(
                webAuthPassword = password,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(targetAuthPassword = password.takeUnlessEmpty())
        }
    }

    fun onReplaceLocalChanged(replaceLocal: Boolean) = runAction {
        updateViewState {
            copy(
                replaceLocal = replaceLocal,
                hasChanged = true,
            )
        }
        updateConfig {
            copy(replaceLocal = replaceLocal)
        }
    }

    fun onBackPressed() = runAction {
        closeScreen(NavigationDestination.SyncImport.RESULT_CHANGED)
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.SYNC_DOCUMENTATION)
    }
}
