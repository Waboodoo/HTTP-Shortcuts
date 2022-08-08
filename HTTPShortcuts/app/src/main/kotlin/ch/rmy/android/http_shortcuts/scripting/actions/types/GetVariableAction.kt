package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class GetVariableAction(val variableKeyOrId: VariableKeyOrId) : BaseAction() {

    @Inject
    lateinit var variableResolver: VariableResolver

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun executeForValue(executionContext: ExecutionContext): Single<Any> =
        getVariableValue(variableKeyOrId, executionContext.variableManager)
            .onErrorResumeNext { error ->
                if (error is VariableNotFoundException) {
                    resolveVariable(variableKeyOrId, executionContext.variableManager)
                        .andThen(getVariableValue(variableKeyOrId, executionContext.variableManager))
                } else {
                    Single.error(error)
                }
            }
            .onErrorResumeNext { error ->
                Single.error(
                    if (error is VariableNotFoundException) {
                        ActionException {
                            it.getString(R.string.error_variable_not_found_read, variableKeyOrId)
                        }
                    } else {
                        error
                    }
                )
            }

    private fun getVariableValue(variableKeyOrId: VariableKeyOrId, variableManager: VariableManager): Single<Any> =
        Single.fromCallable {
            variableManager.getVariableValueByKeyOrId(variableKeyOrId)
                ?: throw VariableNotFoundException()
        }

    private fun resolveVariable(variableKeyOrId: VariableKeyOrId, variableManager: VariableManager): Completable =
        Single.fromCallable {
            variableManager.getVariableByKeyOrId(variableKeyOrId)
                ?: throw VariableNotFoundException()
        }
            .flatMapCompletable { variable ->
                variableResolver.resolveVariables(
                    variablesToResolve = listOf(variable),
                    preResolvedValues = variableManager.getVariableValues(),
                )
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .flatMapCompletable { resolvedVariables ->
                        Completable.fromAction {
                            resolvedVariables.forEach { (variable, value) ->
                                variableManager.setVariableValue(variable, value)
                            }
                        }
                    }
            }

    private class VariableNotFoundException : Throwable()
}
