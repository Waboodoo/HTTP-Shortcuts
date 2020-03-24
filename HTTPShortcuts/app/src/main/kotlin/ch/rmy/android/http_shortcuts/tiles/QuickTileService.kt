package ch.rmy.android.http_shortcuts.tiles

import android.content.Intent
import android.service.quicksettings.TileService
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.ThemeHelper

class QuickTileService : TileService() {

    override fun onClick() {
        val shortcuts = getShortcuts()

        when (shortcuts.size) {
            0 -> {
                showInstructions()
            }
            1 -> {
                executeShortcut(shortcuts[0].id)
            }
            2 -> {
                showPickerDialog(shortcuts)
            }
        }
    }

    private fun getShortcuts() =
        Controller().use { controller ->
            controller.getShortcuts()
                .filter { it.quickSettingsTileShortcut }
                .map { it.detachFromRealm() }
        }

    private fun showInstructions() {
        applyTheme()
        val dialog = DialogBuilder(context)
            .message(getString(
                R.string.instructions_quick_settings_tile,
                getString(R.string.label_quick_tile_shortcut),
                getString(R.string.label_misc_settings)
            ))
            .positive(R.string.dialog_ok)
            .build()
        tryOrLog {
            showDialog(dialog)
        }
    }

    private fun showPickerDialog(shortcuts: List<Shortcut>) {
        applyTheme()
        val dialog = DialogBuilder(context)
            .mapFor(shortcuts) { builder, shortcut ->
                builder.item(shortcut.name) {
                    executeShortcut(shortcut.id)
                }
            }
            .build()
        tryOrLog {
            showDialog(dialog)
        }
    }

    private fun applyTheme() {
        setTheme(ThemeHelper(context).theme)
    }

    private fun executeShortcut(shortcutId: String) {
        ExecuteActivity.IntentBuilder(context, shortcutId)
            .build()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let { intent ->
                startActivityAndCollapse(intent)
            }
    }

}