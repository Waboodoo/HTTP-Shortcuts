package ch.rmy.android.http_shortcuts.tiles

import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import android.view.WindowManager
import androidx.annotation.RequiresApi
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionFactory
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.usecases.CheckHeadlessExecutionUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.ThemeHelper
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var checkHeadlessExecution: CheckHeadlessExecutionUseCase

    override fun onCreate() {
        super.onCreate()
        context.getApplicationComponent().inject(this)
    }

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
        when (shortcuts.size) {
            0 -> showInstructions()
            1 -> executeShortcut(shortcuts.first())
            else -> showPickerDialog(shortcuts)
        }
    }

    private fun showInstructions() {
        applyTheme()
        DialogBuilder(context)
            .message(
                getString(
                    R.string.instructions_quick_settings_tile,
                    getString(R.string.label_quick_tile_shortcut),
                    getString(R.string.label_execution_settings),
                )
            )
            .positive(R.string.dialog_ok)
            .build()
            .showInService()
    }

    private fun showPickerDialog(shortcuts: List<Shortcut>) {
        applyTheme()
        DialogBuilder(context)
            .runFor(shortcuts) { shortcut ->
                item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                    executeShortcut(shortcut)
                }
            }
            .build()
            .showInService()
    }

    private fun Dialog.showInService() {
        try {
            showDialog(this)
        } catch (e: WindowManager.BadTokenException) {
            // Ignore
        } catch (e: Throwable) {
            logException(e)
        }
    }

    private fun applyTheme() {
        setTheme(ThemeHelper(context).theme)
    }

    private fun executeShortcut(shortcut: Shortcut) {
        if (shortcut.canRunWithoutExecuteActivity()) {
            scope.launch {
                executionFactory.createExecution(
                    ExecutionParams(
                        shortcutId = shortcut.id,
                        trigger = ShortcutTriggerType.QUICK_SETTINGS_TILE,
                    )
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
