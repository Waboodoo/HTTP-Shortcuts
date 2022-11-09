package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class ChangeDescriptionAction(private val description: String, private val shortcutNameOrId: ShortcutNameOrId?) : BaseAction() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        changeDescription(
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )
    }

    private suspend fun changeDescription(shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager) {
        val newDescription = Variables.rawPlaceholdersToResolvedValues(description, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(ShortcutModel.DESCRIPTION_MAX_LENGTH)
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
}
