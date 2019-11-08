package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.truncate
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import com.android.volley.VolleyError
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers

class RenameShortcutAction(
    actionType: RenameShortcutActionType,
    data: Map<String, String>
) : BaseAction(actionType, data) {

    var name
        get() = internalData[KEY_NAME] ?: ""
        set(value) {
            internalData[KEY_NAME] = value
        }

    val shortcutNameOrId = data[KEY_SHORTCUT_NAME_OR_ID]

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, volleyError: VolleyError?, recursionDepth: Int): Completable =
        renameShortcut(context, this.shortcutNameOrId ?: shortcutId, variableManager)

    private fun renameShortcut(context: Context, shortcutNameOrId: String, variableManager: VariableManager): Completable {
        val newName = Variables.rawPlaceholdersToResolvedValues(name, variableManager.getVariableValuesByIds())
        if (newName.isEmpty()) {
            return Completable.complete()
        }
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId)
        if (shortcut == null) {
            return Completable.fromAction {
                context.showToast(String.format(context.getString(R.string.error_shortcut_not_found_for_renaming), shortcutNameOrId), long = true)
            }
                .subscribeOn(AndroidSchedulers.mainThread())
        }
        return renameShortcut(shortcut.id, newName)
            .andThen(Completable.fromAction {
                if (LauncherShortcutManager.supportsPinning(context)) {
                    LauncherShortcutManager.updatePinnedShortcut(
                        context = context,
                        shortcutId = shortcut.id,
                        shortcutName = newName,
                        shortcutIcon = shortcut.iconName
                    )
                }
            })
    }

    companion object {

        const val KEY_NAME = "name"
        const val KEY_SHORTCUT_NAME_OR_ID = "shortcut_id"

        private fun renameShortcut(shortcutId: String, newName: String) =
            Transactions.commit { realm ->
                Repository.getShortcutById(realm, shortcutId)?.name = newName.truncate(40)
            }

    }

}