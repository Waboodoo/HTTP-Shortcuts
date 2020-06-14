package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import io.reactivex.Completable

class ChangeIconAction(private val icon: String, private val shortcutNameOrId: String?) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        changeIcon(executionContext.context, this.shortcutNameOrId ?: executionContext.shortcutId)

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

        private fun changeIcon(shortcutId: String, newIcon: String) =
            Transactions.commit { realm ->
                Repository.getShortcutById(realm, shortcutId)?.iconName = newIcon
            }

    }

}