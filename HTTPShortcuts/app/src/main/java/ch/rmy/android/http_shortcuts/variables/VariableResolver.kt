package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.utils.filter
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.rejectSafely
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.SyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import org.jdeferred2.DonePipe
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class VariableResolver(private val context: Context) {

    fun resolve(controller: Controller, shortcut: Shortcut, preResolvedValues: Map<String, String> = emptyMap()): Promise<Map<String, String>, Unit, Unit> {
        val variableMap = controller.getVariables().associate { it.key to it }
        val requiredVariableKeys = extractVariableKeys(shortcut)
        val variablesToResolve = requiredVariableKeys.mapNotNull { variableMap[it] }
        return resolveVariables(controller, variablesToResolve, preResolvedValues)
                .then(DonePipe<Map<String, String>, Map<String, String>, Unit, Unit> { resolvedVariables ->
                    return@DonePipe resolveRecursiveVariables(controller, variableMap, resolvedVariables)
                })
                .filter { resolvedValues ->
                    resolvedValues.mapValues { entry ->
                        val variable = variableMap[entry.key] ?: return@mapValues entry.value
                        encodeValue(variable, entry.value)
                    }
                }
    }

    private fun resolveRecursiveVariables(controller: Controller, variableMap: Map<String, Variable>, preResolvedValues: Map<String, String>, recursionDepth: Int = 0): Promise<Map<String, String>, Unit, Unit> {
        val requiredVariableKeys = mutableSetOf<String>()
        preResolvedValues.values.forEach { value ->
            requiredVariableKeys.addAll(Variables.extractVariableKeys(value))
        }
        if (recursionDepth >= MAX_RECURSION_DEPTH || requiredVariableKeys.isEmpty()) {
            return PromiseUtils.resolve(preResolvedValues)
        }

        val variablesToResolve = requiredVariableKeys.mapNotNull { variableMap[it] }
        return resolveVariables(controller, variablesToResolve, preResolvedValues)
                .filter {
                    it.toMutableMap().also { resolvedVariables ->
                        resolvedVariables.forEach { resolvedVariable ->
                            resolvedVariables[resolvedVariable.key] = Variables.rawPlaceholdersToResolvedValues(resolvedVariable.value, resolvedVariables)
                        }
                    }
                }
                .then(DonePipe { resolvedVariables ->
                    resolveRecursiveVariables(controller, variableMap, resolvedVariables, recursionDepth + 1)
                })
    }

    private fun resolveVariables(controller: Controller, variablesToResolve: List<Variable>, preResolvedValues: Map<String, String> = emptyMap()): Promise<Map<String, String>, Unit, Unit> {
        val deferred = DeferredObject<Map<String, String>, Unit, Unit>()
        val resolvedVariables = preResolvedValues.toMutableMap()

        val waitingDialogs = mutableListOf<() -> Unit>()
        var i = 0
        for (variable in variablesToResolve) {
            if (resolvedVariables.containsKey(variable.key)) {
                continue
            }

            val variableType = VariableTypeFactory.getType(variable.type)
            if (variableType is AsyncVariableType) {
                val index = i++

                val deferredValue = DeferredObject<String, Unit, Unit>()
                deferredValue
                        .done { value ->
                            resolvedVariables[variable.key] = value

                            if (index + 1 >= waitingDialogs.size) {
                                deferred.resolve(resolvedVariables)
                            } else {
                                waitingDialogs[index + 1]()
                            }
                        }
                        .fail {
                            deferred.rejectSafely(Unit)
                        }

                val dialog = variableType.createDialog(context, controller, variable, deferredValue)
                waitingDialogs.add(dialog)
            } else if (variableType is SyncVariableType) {
                resolvedVariables[variable.key] = variableType.resolveValue(controller, variable)
            }
        }

        if (waitingDialogs.isEmpty()) {
            deferred.resolve(resolvedVariables)
        } else {
            waitingDialogs.first().invoke()
        }

        return deferred
                .promise()
                .always { _, _, _ ->
                    resetVariableValues(controller, variablesToResolve)
                }
    }

    private fun resetVariableValues(controller: Controller, variables: List<Variable>) =
            controller.resetVariableValues(variables
                    .filter { it.isResetAfterUse() }
                    .map { it.id }
            )

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

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
                    addAll(extractVariableKeys(shortcut.beforeActions))
                    addAll(extractVariableKeys(shortcut.successActions))
                    addAll(extractVariableKeys(shortcut.failureActions))
                }

        private fun extractVariableKeys(actions: List<ActionDTO>) =
                actions.flatMap {
                    it.data.values
                            .map { Variables.extractVariableKeys(it) }
                            .flatten()
                }

        private fun encodeValue(variable: Variable, value: String) =
                value
                        .mapIf(variable.jsonEncode) {
                            JSONObject.quote(it).drop(1).dropLast(1)
                        }
                        .mapIf(variable.urlEncode) {
                            try {
                                URLEncoder.encode(it, "utf-8")
                            } catch (e: UnsupportedEncodingException) {
                                it
                            }
                        }

    }

}
