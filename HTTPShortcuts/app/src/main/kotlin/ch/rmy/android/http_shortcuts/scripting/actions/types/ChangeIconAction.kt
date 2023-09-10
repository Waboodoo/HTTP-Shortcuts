package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import javax.inject.Inject

class ChangeIconAction
@Inject
constructor(
    private val context: Context,
    private val shortcutRepository: ShortcutRepository,
    private val widgetManager: WidgetManager,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
) : Action<ChangeIconAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) =
        changeIcon(this.shortcutNameOrId ?: executionContext.shortcutId)

    private suspend fun Params.changeIcon(shortcutNameOrId: ShortcutNameOrId) {
        val newIcon = ShortcutIcon.fromName(iconName)
        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_changing_icon, shortcutNameOrId)
            }
        }

        shortcutRepository.setIcon(shortcut.id, newIcon)

        launcherShortcutUpdater.updatePinnedShortcut(shortcut.id)
        widgetManager.updateWidgets(context, shortcut.id)
    }

    data class Params(
        val iconName: String,
        val shortcutNameOrId: ShortcutNameOrId?,
    )
}
