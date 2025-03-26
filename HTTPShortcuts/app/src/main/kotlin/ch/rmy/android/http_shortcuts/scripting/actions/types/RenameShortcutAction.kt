package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.Constants
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import javax.inject.Inject

class RenameShortcutAction
@Inject
constructor(
    private val context: Context,
    private val shortcutRepository: ShortcutRepository,
    private val widgetManager: WidgetManager,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
) : Action<RenameShortcutAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        renameShortcut(
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )
    }

    private suspend fun Params.renameShortcut(shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager) {
        val newName = Variables.rawPlaceholdersToResolvedValues(name, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(Constants.SHORTCUT_NAME_MAX_LENGTH)
        if (newName.isEmpty()) {
            return
        }

        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
        } catch (_: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_renaming, shortcutNameOrId)
            }
        }
        shortcutRepository.setName(shortcut.id, newName)

        launcherShortcutUpdater.updatePinnedShortcut(shortcut.id)
        widgetManager.updateWidgets(context, shortcut.id)
    }

    data class Params(
        val name: String,
        val shortcutNameOrId: ShortcutNameOrId?,
    )
}
