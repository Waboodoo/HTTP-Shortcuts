package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.Constants
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class ChangeDescriptionAction
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
) : Action<ChangeDescriptionAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        changeDescription(
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )
    }

    private suspend fun Params.changeDescription(shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager) {
        val newDescription = Variables.rawPlaceholdersToResolvedValues(description, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(Constants.SHORTCUT_DESCRIPTION_MAX_LENGTH)
        if (newDescription.isEmpty()) {
            return
        }

        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                getString(R.string.error_shortcut_not_found_for_changing_description, shortcutNameOrId)
            }
        }
        shortcutRepository.setDescription(shortcut.id, newDescription)
    }

    data class Params(
        val description: String,
        val shortcutNameOrId: ShortcutNameOrId?,
    )
}
