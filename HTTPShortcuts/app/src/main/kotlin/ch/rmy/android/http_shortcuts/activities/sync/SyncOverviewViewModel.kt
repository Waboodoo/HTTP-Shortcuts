package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncOverviewViewModel
@Inject
constructor(
    application: Application,
) : BaseViewModel<Unit, SyncOverviewViewState>(application) {
    override suspend fun initialize(data: Unit): SyncOverviewViewState = SyncOverviewViewState(
        syncType = SyncType.DISABLED,
    )

    fun onSyncTypeSelected(syncType: SyncType) = runAction {
        updateViewState {
            copy(syncType = syncType)
        }
    }

    fun onConfigureClicked() = runAction {
        // TODO
    }
}
