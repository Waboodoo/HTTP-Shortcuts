package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable
import javax.inject.Inject

class SetVariableAction(val variableKeyOrId: VariableKeyOrId, val value: String) : BaseAction() {

    @Inject
    lateinit var variableRepository: VariableRepository

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.defer {
            executionContext.variableManager.setVariableValueByKeyOrId(variableKeyOrId, value)
            variableRepository.getVariableByKeyOrId(variableKeyOrId)
                .flatMapCompletable { variable ->
                    variableRepository.setVariableValue(variable.id, value.truncate(MAX_VARIABLE_LENGTH))
                }
                .onErrorResumeNext { error ->
                    Completable.error(
                        if (error is NoSuchElementException) {
                            ActionException {
                                it.getString(
                                    R.string.error_variable_not_found_write,
                                    variableKeyOrId,
                                )
                            }
                        } else error
                    )
                }
        }

    companion object {

        private const val MAX_VARIABLE_LENGTH = 30000
    }
}
