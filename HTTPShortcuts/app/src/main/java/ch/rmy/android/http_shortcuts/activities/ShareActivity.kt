package ch.rmy.android.http_shortcuts.activities

import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.mapFor
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.afollestad.materialdialogs.MaterialDialog

class ShareActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.type
        if (type != TYPE_TEXT) {
            finishWithoutAnimation()
            return
        }
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (text == null) {
            showInstructions(R.string.error_sharing_feature_not_available_yet)
            return
        }

        val controller = destroyer.own(Controller())
        val variables = getTargetableVariables(controller)
        val shortcuts = getTargetableShortcuts(controller, variables)

        val variableValues = variables.associate { variable ->
            variable to text
        }

        when (shortcuts.size) {
            0 -> showInstructions(R.string.error_not_suitable_shortcuts)
            1 -> {
                executeShortcut(shortcuts[0], variableValues)
                finishWithoutAnimation()
            }
            else -> showShortcutSelection(shortcuts, variableValues)
        }
    }

    private fun getTargetableVariables(controller: Controller) =
            controller
                    .getVariables()
                    .filter { it.isShareText }
                    .map { it.key }
                    .toSet()

    private fun getTargetableShortcuts(controller: Controller, variableKeys: Set<String>): List<Shortcut> =
            controller
                    .getShortcuts()
                    .filter { hasShareVariable(it, variableKeys) }

    private fun hasShareVariable(shortcut: Shortcut, variableKeys: Set<String>): Boolean {
        val variableKeysInShortcut = VariableResolver.extractVariableKeys(shortcut)
        return variableKeys.any { variableKeysInShortcut.contains(it) }
    }

    private fun executeShortcut(shortcut: Shortcut, variableValues: Map<String, String>) {
        val intent = ExecuteActivity.IntentBuilder(context, shortcut.id)
                .variableValues(variableValues)
                .build()
        startActivity(intent)
    }

    private fun showInstructions(@StringRes text: Int) {
        MaterialDialog.Builder(context)
                .content(text)
                .dismissListener { finishWithoutAnimation() }
                .positiveText(R.string.dialog_ok)
                .showIfPossible()
    }

    private fun showShortcutSelection(shortcuts: List<Shortcut>, variableValues: Map<String, String>) {
        MenuDialogBuilder(context)
                .mapFor(shortcuts) { builder, shortcut ->
                    builder.item(shortcut.name) {
                        executeShortcut(shortcut, variableValues)
                    }
                }
                .dismissListener { finishWithoutAnimation() }
                .showIfPossible()
    }

    companion object {

        private const val TYPE_TEXT = "text/plain"

    }

}
