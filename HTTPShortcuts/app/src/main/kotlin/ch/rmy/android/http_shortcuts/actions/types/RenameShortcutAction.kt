package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable

class RenameShortcutAction(
    actionType: RenameShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var name
        get() = internalData[KEY_NAME] ?: ""
        set(value) {
            internalData[KEY_NAME] = value
        }

    val shortcutId = data[KEY_SHORTCUT_ID]

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable =
        renameShortcut(context, this.shortcutId ?: shortcutId, variableManager)

    private fun renameShortcut(context: Context, shortcutId: String, variableManager: VariableManager): Completable {
        Controller().use { controller ->
            val newName = Variables.rawPlaceholdersToResolvedValues(name, variableManager.getVariableValuesByIds())
            if (newName.isEmpty()) {
                return Completable.complete()
            }
            return controller.renameShortcut(shortcutId, newName)
                .andThen {
                    val shortcut = controller.getShortcutById(shortcutId)
                    if (LauncherShortcutManager.supportsPinning(context) && shortcut != null) {
                        LauncherShortcutManager.updatePinnedShortcut(
                            context = context,
                            shortcutId = shortcut.id,
                            shortcutName = shortcut.name,
                            shortcutIcon = shortcut.iconName
                        )
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