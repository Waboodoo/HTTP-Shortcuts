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
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.ThemeHelper
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate() {
        super.onCreate()
        context.getApplicationComponent().inject(this)
    }

    override fun onClick() {
        val shortcuts = getShortcuts()

        when (shortcuts.size) {
            0 -> {
                showInstructions()
            }
            1 -> {
                executeShortcut(shortcuts[0].id)
            }
            else -> {
                showPickerDialog(shortcuts)
            }
        }
    }

    private fun getShortcuts() =
        runBlocking {
            shortcutRepository.getShortcuts()
        }
            .sortedBy { it.name }
            .filter { it.quickSettingsTileShortcut }

    private fun showInstructions() {
        applyTheme()
        val dialog = DialogBuilder(activityProvider.getActivity())
            .message(
                getString(
                    R.string.instructions_quick_settings_tile,
                    getString(R.string.label_quick_tile_shortcut),
                    getString(R.string.label_execution_settings),
                )
            )
            .positive(R.string.dialog_ok)
            .build()
        show(dialog)
    }

    private fun showPickerDialog(shortcuts: List<ShortcutModel>) {
        applyTheme()
        val dialog = DialogBuilder(activityProvider.getActivity())
            .runFor(shortcuts) { shortcut ->
                item(name = shortcut.name, shortcutIcon = shortcut.icon) {
                    executeShortcut(shortcut.id)
                }
            }
            .build()
        show(dialog)
    }

    private fun show(dialog: Dialog) {
        try {
            showDialog(dialog)
        } catch (e: WindowManager.BadTokenException) {
            // Ignore
        } catch (e: Throwable) {
            logException(e)
        }
    }

    private fun applyTheme() {
        setTheme(ThemeHelper(context).theme)
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        ExecuteActivity.IntentBuilder(shortcutId)
            .build(context)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { intent ->
                startActivityAndCollapse(intent)
            }
    }

    override fun onStartListening() {
        super.onStartListening()
        val shortcuts = getShortcuts()
        qsTile?.label = when (shortcuts.size) {
            1 -> shortcuts.first().name
            else -> getString(R.string.action_quick_settings_tile_trigger)
        }
        qsTile?.updateTile()
    }
}
