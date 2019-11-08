package ch.rmy.android.http_shortcuts.activities.misc

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.variables.VariableLookup
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver

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
        val variableLookup = VariableManager(controller.getVariables())
        val variables = getTargetableVariables(controller)
        val variableIds = variables.map { it.id }.toSet()
        val shortcuts = getTargetableShortcuts(controller, variableIds, variableLookup)

        val variableValues = variables.associate { variable -> variable.key to text }

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
            .toSet()

    private fun getTargetableShortcuts(controller: Controller, variableIds: Set<String>, variableLookup: VariableLookup): List<Shortcut> =
        controller
            .getShortcuts()
            .filter { hasShareVariable(it, variableIds, variableLookup) }

    private fun hasShareVariable(shortcut: Shortcut, variableIds: Set<String>, variableLookup: VariableLookup): Boolean {
        val variableIdsInShortcut = VariableResolver.extractVariableIds(shortcut, variableLookup)
        return variableIds.any { variableIdsInShortcut.contains(it) }
    }

    private fun executeShortcut(shortcut: Shortcut, variableValues: Map<String, String>) {
        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .variableValues(variableValues)
            .build()
            .startActivity(this)
    }

    private fun showInstructions(@StringRes text: Int) {
        DialogBuilder(context)
            .message(text)
            .dismissListener { finishWithoutAnimation() }
            .positive(R.string.dialog_ok)
            .showIfPossible()
    }

    private fun showShortcutSelection(shortcuts: List<Shortcut>, variableValues: Map<String, String>) {
        DialogBuilder(context)
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
