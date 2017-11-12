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
import java.util.*

class VariableResolver(private val context: Context) {

    fun resolve(shortcut: Shortcut, variables: List<Variable>, preResolvedValues: Map<String, String>?): Promise<ResolvedVariables, Void, Void> {
        val requiredVariableNames = extractVariableKeys(shortcut)
        val variablesToResolve = filterVariablesByName(variables, requiredVariableNames)
        return resolveVariables(variablesToResolve, preResolvedValues)
    }

    private fun filterVariablesByName(variables: List<Variable>, variableNames: Collection<String>): List<Variable> {
        val filteredVariables = ArrayList<Variable>()
        for (variable in variables) {
            if (variableNames.contains(variable.key)) {
                filteredVariables.add(variable)
            }
        }
        return filteredVariables
    }

    private fun resolveVariables(variablesToResolve: List<Variable>, preResolvedValues: Map<String, String>?): Promise<ResolvedVariables, Void, Void> {
        val controller = Controller()
        val deferred = DeferredObject<ResolvedVariables, Void, Void>()
        val builder = ResolvedVariables.Builder()

        val waitingDialogs = ArrayList<() -> Unit>()
        var i = 0
        for (variable in variablesToResolve) {
            if (preResolvedValues != null && preResolvedValues.containsKey(variable.key)) {
                builder.add(variable, preResolvedValues[variable.key]!!)
                continue
            }

            val variableType = TypeFactory.getType(variable.type!!)

            if (variableType is AsyncVariableType) {
                val index = i++

                val deferredValue = DeferredObject<String, Void, Void>()
                deferredValue.done { result ->
                    builder.add(variable, result)

                    if (index + 1 >= waitingDialogs.size) {
                        deferred.resolve(builder.build())
                    } else {
                        waitingDialogs[index + 1]()
                    }
                }.fail {
                    deferred.reject(null)
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
            waitingDialogs[0]()
        }

        return deferred.promise().always { _, _, _ ->
            resetVariableValues(controller, variablesToResolve)
            controller.destroy()
        }
    }

    private fun resetVariableValues(controller: Controller, variables: List<Variable>) {
        for (variable in variables) {
            if (variable.isResetAfterUse()) {
                controller.setVariableValue(variable, "")
            }
        }
    }

    companion object {

        fun extractVariableKeys(shortcut: Shortcut): Set<String> {
            val discoveredVariables = HashSet<String>()

            discoveredVariables.addAll(Variables.extractVariableNames(shortcut.url!!))
            discoveredVariables.addAll(Variables.extractVariableNames(shortcut.username!!))
            discoveredVariables.addAll(Variables.extractVariableNames(shortcut.password!!))
            discoveredVariables.addAll(Variables.extractVariableNames(shortcut.bodyContent!!))

            if (Shortcut.METHOD_GET != shortcut.method) {
                for (parameter in shortcut.parameters!!) {
                    discoveredVariables.addAll(Variables.extractVariableNames(parameter.key!!))
                    discoveredVariables.addAll(Variables.extractVariableNames(parameter.value!!))
                }
            }
            for (header in shortcut.headers!!) {
                discoveredVariables.addAll(Variables.extractVariableNames(header.key!!))
                discoveredVariables.addAll(Variables.extractVariableNames(header.value!!))
            }

            return discoveredVariables
        }
    }

}
