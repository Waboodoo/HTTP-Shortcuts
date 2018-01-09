package ch.rmy.android.http_shortcuts.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.afollestad.materialdialogs.MaterialDialog
import java.util.*

class ShareActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.type
        if (TYPE_TEXT != type) {
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

        val variableValues = HashMap<String, String>()
        for (variable in variables) {
            variableValues.put(variable, text)
        }

        if (shortcuts.size == 1) {
            executeShortcut(shortcuts[0], variableValues)
            finishWithoutAnimation()
        } else if (shortcuts.isEmpty()) {
            showInstructions(R.string.error_not_suitable_shortcuts)
        } else {
            showShortcutSelection(shortcuts, variableValues)
        }
    }

    private fun getTargetableVariables(controller: Controller): Set<String> {
        val targetableVariables = HashSet<String>()
        val variables = controller.variables
        for (variable in variables) {
            if (variable.isShareText) {
                targetableVariables.add(variable.key!!)
            }
        }
        return targetableVariables
    }

    private fun getTargetableShortcuts(controller: Controller, variableKeys: Set<String>): List<Shortcut> =
            controller.shortcuts.filter { hasShareVariable(it, variableKeys) }

    private fun hasShareVariable(shortcut: Shortcut, variableKeys: Set<String>): Boolean {
        val variableKeysInShortcut = VariableResolver.extractVariableKeys(shortcut)
        return variableKeys.any { variableKeysInShortcut.contains(it) }
    }

    private fun executeShortcut(shortcut: Shortcut, variableValues: HashMap<String, String>) {
        val intent = IntentUtil.createIntent(context, shortcut.id, variableValues)
        startActivity(intent)
    }

    private fun showInstructions(@StringRes text: Int) {
        MaterialDialog.Builder(context)
                .content(text)
                .dismissListener { finishWithoutAnimation() }
                .positiveText(R.string.button_ok)
                .show()
    }

    private fun showShortcutSelection(shortcuts: List<Shortcut>, variableValues: HashMap<String, String>) {
        val builder = MenuDialogBuilder(context)
        for (shortcut in shortcuts) {
            builder.item(shortcut.name!!, {
                executeShortcut(shortcut, variableValues)
            })
        }
        builder.dismissListener(DialogInterface.OnDismissListener { finishWithoutAnimation() }).show()
    }

    companion object {

        private const val TYPE_TEXT = "text/plain"
    }

}
