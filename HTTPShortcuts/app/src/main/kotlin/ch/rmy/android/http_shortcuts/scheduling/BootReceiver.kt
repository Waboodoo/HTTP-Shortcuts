package ch.rmy.android.http_shortcuts.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.sync.SyncScheduler
import ch.rmy.android.http_shortcuts.tiles.QuickTileUpdater
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    @Inject
    lateinit var syncScheduler: SyncScheduler

    @Inject
    lateinit var quickTileUpdater: QuickTileUpdater

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        CoroutineScope(Dispatchers.Default).launch {
            logInfo("Device rebooted")
            executionScheduler.schedule()
            syncScheduler.schedule()
            quickTileUpdater.update()
        }
    }
}
