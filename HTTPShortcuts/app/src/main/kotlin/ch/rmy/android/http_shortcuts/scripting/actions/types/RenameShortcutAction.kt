package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import javax.inject.Inject

class RenameShortcutAction(private val name: String, private val shortcutNameOrId: ShortcutNameOrId?) : BaseAction() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var widgetManager: WidgetManager

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        renameShortcut(
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )
    }

    private suspend fun renameShortcut(shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager) {
        val newName = Variables.rawPlaceholdersToResolvedValues(name, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(Shortcut.NAME_MAX_LENGTH)
        if (newName.isEmpty()) {
            return
        }

        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_renaming, shortcutNameOrId)
            }
        }
        shortcutRepository.setName(shortcut.id, newName)

        LauncherShortcutManager(context)
            .takeIf { it.supportsPinning() }
            ?.updatePinnedShortcut(
                shortcutId = shortcut.id,
                shortcutName = newName,
                shortcutIcon = shortcut.icon,
            )

        widgetManager.updateWidgets(context, shortcut.id)
    }
}
