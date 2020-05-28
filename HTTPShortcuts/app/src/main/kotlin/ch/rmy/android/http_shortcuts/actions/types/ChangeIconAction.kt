package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.http.ErrorResponse
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import io.reactivex.Completable

class ChangeIconAction(
    actionType: ChangeIconActionType,
    data: Map<String, String>
) : BaseAction(actionType) {

    private val icon = data[KEY_ICON] ?: ""

    private val shortcutNameOrId = data[KEY_SHORTCUT_NAME_OR_ID]?.takeUnless { it.isEmpty() }

    override fun perform(context: Context, shortcutId: String, variableManager: VariableManager, response: ShortcutResponse?, responseError: ErrorResponse?, recursionDepth: Int): Completable =
        changeIcon(context, this.shortcutNameOrId ?: shortcutId)

    private fun changeIcon(context: Context, shortcutNameOrId: String): Completable {
        val newIcon = icon
        val shortcut = DataSource.getShortcutByNameOrId(shortcutNameOrId)
            ?: return Completable
                .error(ActionException {
                    it.getString(R.string.error_shortcut_not_found_for_changing_icon, shortcutNameOrId)
                })
        return changeIcon(shortcut.id, newIcon)
            .andThen(Completable.fromAction {
                if (LauncherShortcutManager.supportsPinning(context)) {
                    LauncherShortcutManager.updatePinnedShortcut(
                        context = context,
                        shortcutId = shortcut.id,
                        shortcutName = shortcut.name,
                        shortcutIcon = newIcon
                    )
                }
                WidgetManager.updateWidgets(context, shortcut.id)
            })
    }

    companion object {

        const val KEY_ICON = "icon"
        const val KEY_SHORTCUT_NAME_OR_ID = "shortcut_id"

        private fun changeIcon(shortcutId: String, newIcon: String) =
            Transactions.commit { realm ->
                Repository.getShortcutById(realm, shortcutId)?.iconName = newIcon
            }

    }

}