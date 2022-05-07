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
import io.reactivex.Completable
import javax.inject.Inject

class ChangeDescriptionAction(private val description: String, private val shortcutNameOrId: ShortcutNameOrId?) : BaseAction() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        changeDescription(
            this.shortcutNameOrId ?: executionContext.shortcutId,
            executionContext.variableManager,
        )

    private fun changeDescription(shortcutNameOrId: ShortcutNameOrId, variableManager: VariableManager): Completable {
        val newDescription = Variables.rawPlaceholdersToResolvedValues(description, variableManager.getVariableValuesByIds())
            .trim()
            .truncate(ShortcutModel.DESCRIPTION_MAX_LENGTH)
        if (newDescription.isEmpty()) {
            return Completable.complete()
        }

        return shortcutRepository.getShortcutByNameOrId(shortcutNameOrId)
            .flatMapCompletable { shortcut ->
                shortcutRepository.setDescription(shortcut.id, newDescription)
            }
            .onErrorResumeNext { error ->
                if (error is NoSuchElementException) {
                    Completable
                        .error(
                            ActionException {
                                it.getString(R.string.error_shortcut_not_found_for_changing_description, shortcutNameOrId)
                            }
                        )
                } else {
                    Completable.error(error)
                }
            }
    }
}
