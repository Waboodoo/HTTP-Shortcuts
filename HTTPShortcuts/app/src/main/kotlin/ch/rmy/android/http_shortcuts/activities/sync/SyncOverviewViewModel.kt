package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.settings.SyncSettings
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncOverviewViewModel
@Inject
constructor(
    application: Application,
    private val syncSettings: SyncSettings,
) : BaseViewModel<Unit, SyncOverviewViewState>(application) {
    override suspend fun initialize(data: Unit): SyncOverviewViewState = SyncOverviewViewState(
        syncType = syncSettings.syncType,
    )

    fun onSyncTypeSelected(syncType: SyncType?) = runAction {
        syncSettings.syncType = syncType
        updateViewState {
            copy(syncType = syncType)
        }
    }

    fun onConfigureClicked() = runAction {
        navigate(NavigationDestination.SyncSettings)
    }
}
