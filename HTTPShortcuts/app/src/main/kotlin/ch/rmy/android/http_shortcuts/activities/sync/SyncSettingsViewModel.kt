package ch.rmy.android.http_shortcuts.activities.sync

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.settings.SyncSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SyncSettingsViewModel
@Inject
constructor(
    application: Application,
    private val syncSettings: SyncSettings,
) : BaseViewModel<Unit, SyncSettingsViewState>(application) {
    override suspend fun initialize(data: Unit) = SyncSettingsViewState(
        syncType = syncSettings.syncType ?: terminateInitialization(),
    )
}
