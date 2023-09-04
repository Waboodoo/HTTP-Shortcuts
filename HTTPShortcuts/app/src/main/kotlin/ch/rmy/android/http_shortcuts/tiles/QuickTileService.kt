package ch.rmy.android.http_shortcuts.tiles

import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionFactory
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile.QuickSettingsTileActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class QuickTileService : TileService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var checkHeadlessExecution: CheckHeadlessExecutionUseCase

    override fun onClick() {
        if (!scope.isActive) {
            logException(IllegalStateException("QuickTileService coroutine scope was inactive"))
            val shortcuts = runBlocking {
                getShortcuts()
            }
            handleShortcuts(shortcuts)
            return
        }
        scope.launch {
            handleShortcuts(getShortcuts())
        }
    }

    private suspend fun getShortcuts() =
        shortcutRepository.getShortcuts()
            .sortedBy { it.name }
            .filter { it.quickSettingsTileShortcut }

    private fun handleShortcuts(shortcuts: List<Shortcut>) {
        shortcuts.singleOrNull()
            ?.let(::executeShortcut)
            ?: run {
                if (shortcuts.isNotEmpty() && shortcuts.all { it.canRunWithoutExecuteActivity() }) {
                    setTheme(R.style.Theme_MaterialComponents_DayNight_NoActionBar)
                    showDialog(
                        AlertDialog.Builder(context)
                            .setItems(shortcuts.map { it.name }.toTypedArray()) { _, index ->
                                executeShortcut(shortcuts[index])
                            }
                            .create()
                    )
                } else {
                    QuickSettingsTileActivity.IntentBuilder()
                        .build(context)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .let { intent ->
                            startActivityAndCollapse(intent)
                        }
                }
            }
    }

    private fun executeShortcut(shortcut: Shortcut) {
        if (shortcut.canRunWithoutExecuteActivity()) {
            scope.launch {
                executionFactory.createExecution(
                    ExecutionParams(
                        shortcutId = shortcut.id,
                        trigger = ShortcutTriggerType.QUICK_SETTINGS_TILE,
                    ),
                    dialogHandle = object : DialogHandle {
                        override suspend fun <T : Any> showDialog(dialogState: ExecuteDialogState<T>): T {
                            logException(IllegalStateException("Headless quick service tile execution tried showing a dialog"))
                            throw CancellationException()
                        }
                    },
                )
                    .execute()
                    .collect()
            }
            return
        }

        ExecuteActivity.IntentBuilder(shortcut.id)
            .trigger(ShortcutTriggerType.QUICK_SETTINGS_TILE)
            .build(context)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { intent ->
                startActivityAndCollapse(intent)
            }
    }

    private fun Shortcut.canRunWithoutExecuteActivity(): Boolean {
        if (confirmationType != null) {
            return false
        }
        if (codeOnPrepare.isNotEmpty()) {
            return false
        }
        if (!checkHeadlessExecution.invoke(this)) {
            return false
        }
        val variableIds = VariableResolver.extractVariableIdsExcludingScripting(this)
        if (variableIds.isNotEmpty()) {
            // If a shortcut uses any variables, we cannot know whether those variables can be resolved
            // without the ExecuteActivity being present, so we have to err on the side of caution.
            return false
        }
        return true
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            val shortcuts = getShortcuts()
            qsTile?.label = when (shortcuts.size) {
                1 -> shortcuts.first().name
                else -> getString(R.string.action_quick_settings_tile_trigger)
            }
            qsTile?.updateTile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
