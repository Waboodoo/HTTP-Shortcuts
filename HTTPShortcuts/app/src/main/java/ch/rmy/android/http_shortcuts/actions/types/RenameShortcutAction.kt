package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import org.jdeferred2.Promise

class RenameShortcutAction(
        id: String,
        actionType: RenameShortcutActionType,
        data: Map<String, String>
) : BaseAction(id, actionType, data) {

    var name
        get() = internalData[KEY_NAME] ?: ""
        set(value) {
            internalData[KEY_NAME] = value
        }

    val shortcutId = data[KEY_SHORTCUT_ID]?.toLongOrNull()

    override fun getDescription(context: Context): CharSequence =
            Variables.rawPlaceholdersToVariableSpans(context, context.getString(R.string.action_type_rename_shortcut_description, name))

    override fun perform(context: Context, shortcutId: Long, variableValues: MutableMap<String, String>, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Promise<Unit, Throwable, Unit> =
            renameShortcut(context, this.shortcutId ?: shortcutId, variableValues)

    private fun renameShortcut(context: Context, shortcutId: Long, variableValues: Map<String, String>): Promise<Unit, Throwable, Unit> {
        Controller().use { controller ->
            val newName = Variables.rawPlaceholdersToResolvedValues(name, variableValues)
            if (newName.isEmpty()) {
                return PromiseUtils.resolve(Unit)
            }
            return controller.renameShortcut(shortcutId, newName)
                    .done {
                        val shortcut = controller.getShortcutById(shortcutId)
                        if (LauncherShortcutManager.supportsPinning(context) && shortcut != null) {
                            LauncherShortcutManager.updatePinnedShortcut(context, shortcut)
                        }
                    }
        }
    }

    override fun createEditorView(context: Context, variablePlaceholderProvider: VariablePlaceholderProvider) =
            RenameShortcutActionEditorView(context, this, variablePlaceholderProvider)

    companion object {

        const val KEY_NAME = "name"
        const val KEY_SHORTCUT_ID = "shortcut_id"

    }

}