package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncOverviewViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
) : BaseViewModel<Unit, SyncOverviewViewState>(application) {
    override suspend fun initialize(data: Unit): SyncOverviewViewState = SyncOverviewViewState(
        syncType = userPreferences.syncType,
    )

    fun onSyncTypeSelected(syncType: SyncType?) = runAction {
        userPreferences.syncType = syncType
        updateViewState {
            copy(syncType = syncType)
        }
    }

    fun onConfigureImportClicked() = runAction {
        navigate(NavigationDestination.SyncImport)
    }

    fun onConfigureExportClicked() = runAction {
        navigate(NavigationDestination.SyncExport)
    }
}
