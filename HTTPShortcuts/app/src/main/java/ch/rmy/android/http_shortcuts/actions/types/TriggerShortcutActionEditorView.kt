package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class TriggerShortcutActionEditorView(
        context: Context,
        private val action: TriggerShortcutAction
) : BaseActionEditorView(context, R.layout.action_editor_trigger_shortcut) {

    private val controller by lazy {
        destroyer.own(Controller())
    }

    private val shortcutNameView: TextView by bindView(R.id.shortcut_name)

    init {
        updateViews()
        shortcutNameView.setOnClickListener {
            showSelectionDialog()
        }
    }

    private fun updateViews() {
        shortcutNameView.text = action.shortcutName ?: context.getString(R.string.action_target_shortcut_no_shortcut_selected)
    }

    private fun showSelectionDialog() {
        val shortcuts = controller.getShortcuts()

        if (shortcuts.isEmpty()) {
            MaterialDialog.Builder(context)
                    .content(R.string.action_target_shortcut_error_no_shortcuts)
                    .positiveText(R.string.dialog_ok)
                    .showIfPossible()
        } else {
            MenuDialogBuilder(context)
                    .mapFor(shortcuts) { builder, shortcut ->
                        builder.item(shortcut.name) {
                            action.shortcutId = shortcut.id
                            updateViews()
                        }
                    }
                    .showIfPossible()
        }
    }

    override fun compile(): Boolean {
        if (action.shortcutName == null) {
            return false
        }
        return true
    }

}