package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.SyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.TypeFactory
import org.jdeferred.Promise
import org.jdeferred.impl.DeferredObject

class VariableResolver(private val context: Context) {

    fun resolve(shortcut: Shortcut, variables: List<Variable>, preResolvedValues: Map<String, String>?): Promise<ResolvedVariables, Unit, Unit> {
        val requiredVariableKeys = extractVariableKeys(shortcut)
        val variablesToResolve = filterVariablesByName(variables, requiredVariableKeys)
        return resolveVariables(variablesToResolve, preResolvedValues)
    }

    private fun filterVariablesByName(variables: List<Variable>, variableKeys: Collection<String>) =
            variables.filter { variableKeys.contains(it.key) }

    private fun resolveVariables(variablesToResolve: List<Variable>, preResolvedValues: Map<String, String>?): Promise<ResolvedVariables, Unit, Unit> {
        val controller = Controller()
        val deferred = DeferredObject<ResolvedVariables, Unit, Unit>()
        val builder = ResolvedVariables.Builder()

        val waitingDialogs = mutableListOf<() -> Unit>()
        var i = 0
        for (variable in variablesToResolve) {
            if (preResolvedValues != null && preResolvedValues.containsKey(variable.key)) {
                builder.add(variable, preResolvedValues[variable.key]!!)
                continue
            }

            val variableType = TypeFactory.getType(variable.type)

            if (variableType is AsyncVariableType) {
                val index = i++

                val deferredValue = DeferredObject<String, Unit, Unit>()
                deferredValue
                        .done { result ->
                            builder.add(variable, result)

                            if (index + 1 >= waitingDialogs.size) {
                                deferred.resolve(builder.build())
                            } else {
                                waitingDialogs[index + 1]()
                            }
                        }
                        .fail {
                            deferred.reject(Unit)
                        }

                val dialog = variableType.createDialog(context, controller, variable, deferredValue)

                waitingDialogs.add(dialog)
            } else if (variableType is SyncVariableType) {
                val value = variableType.resolveValue(controller, variable)
                builder.add(variable, value)
            }
        }

        if (waitingDialogs.isEmpty()) {
            deferred.resolve(builder.build())
        } else {
            waitingDialogs.first().invoke()
        }

        return deferred
                .promise()
                .always { _, _, _ ->
                    resetVariableValues(controller, variablesToResolve)
                            .always { _, _, _ ->
                                controller.destroy()
                            }
                }
    }

    private fun resetVariableValues(controller: Controller, variables: List<Variable>) =
            controller.resetVariableValues(variables
                    .filter { it.isResetAfterUse() }
                    .map { it.id }
            )

    companion object {

        fun extractVariableKeys(shortcut: Shortcut): Set<String> =
                mutableSetOf<String>().apply {
                    addAll(Variables.extractVariableKeys(shortcut.url))
                    addAll(Variables.extractVariableKeys(shortcut.username))
                    addAll(Variables.extractVariableKeys(shortcut.password))
                    if (shortcut.usesCustomBody()) {
                        addAll(Variables.extractVariableKeys(shortcut.bodyContent))
                    }
                    if (shortcut.usesRequestParameters()) {
                        for (parameter in shortcut.parameters) {
                            addAll(Variables.extractVariableKeys(parameter.key))
                            addAll(Variables.extractVariableKeys(parameter.value))
                        }
                    }
                    for (header in shortcut.headers) {
                        addAll(Variables.extractVariableKeys(header.key))
                        addAll(Variables.extractVariableKeys(header.value))
                    }
                }
    }

}
