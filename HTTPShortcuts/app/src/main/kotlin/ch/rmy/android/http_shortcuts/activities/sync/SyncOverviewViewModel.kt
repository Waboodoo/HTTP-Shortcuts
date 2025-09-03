package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.sync.SyncConfigMonitor
import ch.rmy.android.http_shortcuts.sync.SyncScheduler
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SyncOverviewViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val syncScheduler: SyncScheduler,
    private val syncRepository: SyncRepository,
    private val syncConfigMonitor: SyncConfigMonitor,
) : BaseViewModel<Unit, SyncOverviewViewState>(application) {
    override suspend fun initialize(data: Unit): SyncOverviewViewState {
        viewModelScope.launch {
            syncConfigMonitor.configurationInProgress()
        }

        viewModelScope.launch {
            syncScheduler.observeState().collect { state ->
                updateViewState {
                    copy(isSyncing = state == WorkInfo.State.RUNNING)
                }
            }
        }

        viewModelScope.launch {
            syncRepository.observeConfig(SyncType.EXPORT).collect { exportConfig ->
                updateViewState {
                    copy(
                        exportLastSucceeded = exportConfig?.lastSucceeded?.toLocalDateTime(),
                        exportLastFailed = exportConfig?.lastFailed?.toLocalDateTime(),
                    )
                }
            }
        }

        viewModelScope.launch {
            syncRepository.observeConfig(SyncType.IMPORT).collect { importConfig ->
                updateViewState {
                    copy(
                        importLastSucceeded = importConfig?.lastSucceeded?.toLocalDateTime(),
                        importLastFailed = importConfig?.lastFailed?.toLocalDateTime(),
                    )
                }
            }
        }

        val syncType = userPreferences.syncType
        val syncConfig = syncType?.let {
            syncRepository.getConfig(it)
        }
        val importConfig = syncConfig?.takeIf { it.type == SyncType.IMPORT } ?: syncRepository.getConfig(SyncType.IMPORT)
        val exportConfig = syncConfig?.takeIf { it.type == SyncType.EXPORT } ?: syncRepository.getConfig(SyncType.EXPORT)
        return SyncOverviewViewState(
            syncType = syncType,
            isConfigValid = syncConfig?.isValid == true,
            importLastSucceeded = importConfig.lastSucceeded?.toLocalDateTime(),
            importLastFailed = importConfig.lastFailed?.toLocalDateTime(),
            exportLastSucceeded = exportConfig.lastSucceeded?.toLocalDateTime(),
            exportLastFailed = exportConfig.lastFailed?.toLocalDateTime(),
        )
    }

    private fun Instant.toLocalDateTime() =
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())

    fun onSyncTypeSelected(syncType: SyncType?) = runAction {
        userPreferences.syncType = syncType
        val syncConfig = syncType?.let {
            syncRepository.getConfig(syncType)
        }
        updateViewState {
            copy(
                syncType = syncType,
                isConfigValid = syncConfig?.isValid == true,
            )
        }
        syncScheduler.schedule()
    }

    fun onConfigureImportClicked() = runAction {
        navigate(NavigationDestination.SyncImport)
    }

    fun onConfigureExportClicked() = runAction {
        navigate(NavigationDestination.SyncExport)
    }

    fun onConfigurationChanged() = runAction {
        syncScheduler.schedule()
        val syncConfig = getCurrentViewState().syncType?.let {
            syncRepository.getConfig(it)
        }
        updateViewState {
            copy(isConfigValid = syncConfig?.isValid == true)
        }
    }

    fun onSyncNowClicked() = runAction {
        if (getCurrentViewState().syncState == SyncState.IDLE) {
            syncScheduler.syncNow()
            viewModelScope.launch {
                val state = syncScheduler.observeState()
                    .first { state ->
                        state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED
                    }
                userPreferences.syncType?.let { syncType ->
                    if (state == WorkInfo.State.SUCCEEDED) {
                        when (syncType) {
                            SyncType.IMPORT -> showSnackbar(R.string.message_sync_import_succeeded)
                            SyncType.EXPORT -> showSnackbar(R.string.message_sync_export_succeeded)
                        }
                    } else if (state == WorkInfo.State.FAILED) {
                        when (syncType) {
                            SyncType.IMPORT -> showSnackbar(R.string.message_sync_import_failed)
                            SyncType.EXPORT -> showSnackbar(R.string.message_sync_export_failed)
                        }
                    }
                }
            }
        }
    }

    fun onFailureInfoClicked() = runAction {
        navigate(NavigationDestination.History)
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.SYNC_DOCUMENTATION)
    }
}
