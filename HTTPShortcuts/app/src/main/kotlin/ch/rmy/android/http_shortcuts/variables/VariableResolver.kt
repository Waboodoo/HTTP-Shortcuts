package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.filter
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.rejectSafely
import ch.rmy.android.http_shortcuts.extensions.resolveSafely
import ch.rmy.android.http_shortcuts.utils.PromiseUtils
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.SyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import io.reactivex.Completable
import org.jdeferred2.DonePipe
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class VariableResolver(private val context: Context) {

    fun resolve(controller: Controller, shortcut: Shortcut, preResolvedValues: Map<String, String> = emptyMap()): Promise<Map<String, String>, Unit, Unit> {
        val variableMap = controller.getVariables().associate { it.id to it }
        val requiredVariableIds = extractVariableIds(shortcut).toMutableSet()

        // Always export all constants
        variableMap.forEach { variableId, variable ->
            if (variable.isConstant) {
                requiredVariableIds.add(variableId)
            }
        }

        val variablesToResolve = requiredVariableIds.mapNotNull { variableMap[it] }
        return resolveVariables(variablesToResolve, preResolvedValues)
            .then(DonePipe<Map<String, String>, Map<String, String>, Unit, Unit> { resolvedVariables ->
                return@DonePipe resolveRecursiveVariables(variableMap, resolvedVariables)
            })
            .filter { resolvedValues ->
                resolvedValues.mapValues { entry ->
                    val variable = variableMap[entry.key] ?: return@mapValues entry.value
                    encodeValue(variable, entry.value)
                }
            }
    }

    private fun resolveRecursiveVariables(variableMap: Map<String, Variable>, preResolvedValues: Map<String, String>, recursionDepth: Int = 0): Promise<Map<String, String>, Unit, Unit> {
        val requiredVariableIds = mutableSetOf<String>()
        preResolvedValues.values.forEach { value ->
            requiredVariableIds.addAll(Variables.extractVariableIds(value))
        }
        if (recursionDepth >= MAX_RECURSION_DEPTH || requiredVariableIds.isEmpty()) {
            return PromiseUtils.resolve(preResolvedValues)
        }

        val variablesToResolve = requiredVariableIds.mapNotNull { variableMap[it] }
        return resolveVariables(variablesToResolve, preResolvedValues)
            .filter {
                it.toMutableMap().also { resolvedVariables ->
                    resolvedVariables.forEach { resolvedVariable ->
                        resolvedVariables[resolvedVariable.key] = Variables.rawPlaceholdersToResolvedValues(resolvedVariable.value, resolvedVariables)
                    }
                }
            }
            .then(DonePipe { resolvedVariables ->
                resolveRecursiveVariables(variableMap, resolvedVariables, recursionDepth + 1)
            })
    }

    private fun resolveVariables(variablesToResolve: List<Variable>, preResolvedValues: Map<String, String> = emptyMap()): Promise<Map<String, String>, Unit, Unit> {
        val deferred = DeferredObject<Map<String, String>, Unit, Unit>()
        val resolvedVariables = mutableMapOf<String, String>()

        val waitingDialogs = mutableListOf<() -> Unit>()
        var i = 0
        for (variable in variablesToResolve) {
            if (resolvedVariables.containsKey(variable.id)) {
                continue
            }
            if (preResolvedValues.containsKey(variable.key)) {
                resolvedVariables[variable.id] = preResolvedValues.getValue(variable.key)
                continue
            }

            val variableType = VariableTypeFactory.getType(variable.type)
            if (variableType is AsyncVariableType) {
                val index = i++

                val deferredValue = DeferredObject<String, Unit, Unit>()
                deferredValue
                    .done { value ->
                        resolvedVariables[variable.id] = value

                        if (index + 1 >= waitingDialogs.size) {
                            deferred.resolveSafely(resolvedVariables)
                        } else {
                            waitingDialogs[index + 1]()
                        }
                    }
                    .fail {
                        deferred.rejectSafely(Unit)
                    }

                val dialog = variableType.createDialog(context, variable, deferredValue)
                waitingDialogs.add(dialog)
            } else if (variableType is SyncVariableType) {
                resolvedVariables[variable.id] = variableType.resolveValue(variable)
            }
        }

        if (waitingDialogs.isEmpty()) {
            deferred.resolveSafely(resolvedVariables)
        } else {
            waitingDialogs.first().invoke()
        }

        return deferred
            .promise()
            .always { _, _, _ ->
                resetVariableValues(variablesToResolve).subscribe()
            }
    }

    private fun resetVariableValues(variables: List<Variable>): Completable =
        Commons.resetVariableValues(
            variables
                .filter { it.isResetAfterUse() }
                .map { it.id }
        )

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        fun extractVariableIds(shortcut: Shortcut): Set<String> =
            mutableSetOf<String>().apply {
                addAll(Variables.extractVariableIds(shortcut.url))
                addAll(Variables.extractVariableIds(shortcut.username))
                addAll(Variables.extractVariableIds(shortcut.password))
                if (shortcut.usesCustomBody()) {
                    addAll(Variables.extractVariableIds(shortcut.bodyContent))
                }
                if (shortcut.usesRequestParameters()) {
                    for (parameter in shortcut.parameters) {
                        addAll(Variables.extractVariableIds(parameter.key))
                        addAll(Variables.extractVariableIds(parameter.value))
                    }
                }
                for (header in shortcut.headers) {
                    addAll(Variables.extractVariableIds(header.key))
                    addAll(Variables.extractVariableIds(header.value))
                }
            }

        private fun extractVariableIds(actions: List<ActionDTO>) =
            actions.flatMap {
                it.data.values
                    .map(Variables::extractVariableIds)
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
