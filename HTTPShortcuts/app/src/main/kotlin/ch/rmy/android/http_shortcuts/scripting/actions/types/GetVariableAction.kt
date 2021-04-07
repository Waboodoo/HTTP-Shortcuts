package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

class GetVariableAction(val variableKeyOrId: String) : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        getVariableValue(variableKeyOrId, executionContext.variableManager)
            .onErrorResumeNext { error ->
                if (error is VariableNotFoundException) {
                    resolveVariable(executionContext.context, variableKeyOrId, executionContext.variableManager)
                        .andThen(getVariableValue(variableKeyOrId, executionContext.variableManager))
                } else {
                    Single.error(error)
                }
            }
            .onErrorResumeNext { error ->
                Single.error(if (error is VariableNotFoundException) {
                    ActionException {
                        it.getString(R.string.error_variable_not_found_read, variableKeyOrId)
                    }
                } else {
                    error
                })
            }

    private fun getVariableValue(variableKeyOrId: String, variableManager: VariableManager) =
        Single.fromCallable {
            variableManager.getVariableValueByKeyOrId(variableKeyOrId)
                ?: throw VariableNotFoundException()
        }

    private fun resolveVariable(context: Context, variableKeyOrId: String, variableManager: VariableManager): Completable =
        Single.fromCallable {
            variableManager.getVariableByKeyOrId(variableKeyOrId)
                ?: throw VariableNotFoundException()
        }
            .flatMapCompletable { variable ->
                VariableResolver(context).resolveVariables(
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
