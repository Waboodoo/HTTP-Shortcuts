package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.SyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import io.reactivex.Completable
import io.reactivex.Single
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class VariableResolver(private val context: Context) {

    fun resolve(controller: Controller, shortcut: Shortcut, preResolvedValues: Map<String, String> = emptyMap()): Single<Map<String, String>> {
        val variableMap = controller.getVariables().associate { it.id to it.detachFromRealm() }
        val requiredVariableIds = extractVariableIds(shortcut).toMutableSet()

        // Always export all constants
        variableMap.forEach { (variableId, variable) ->
            if (variable.isConstant) {
                requiredVariableIds.add(variableId)
            }
        }

        val variablesToResolve = requiredVariableIds.mapNotNull { variableMap[it] }
        return resolveVariables(variablesToResolve, preResolvedValues)
            .flatMap { resolvedVariables ->
                resolveRecursiveVariables(variableMap, resolvedVariables)
            }
            .map { resolvedValues ->
                resolvedValues.mapValues { entry ->
                    variableMap[entry.key]
                        ?.let { variable ->
                            encodeValue(variable, entry.value)
                        }
                        ?: entry.value
                }
            }
    }

    private fun resolveRecursiveVariables(variableMap: Map<String, Variable>, preResolvedValues: Map<String, String>, recursionDepth: Int = 0): Single<Map<String, String>> {
        val requiredVariableIds = mutableSetOf<String>()
        preResolvedValues.values.forEach { value ->
            requiredVariableIds.addAll(Variables.extractVariableIds(value))
        }
        if (recursionDepth >= MAX_RECURSION_DEPTH || requiredVariableIds.isEmpty()) {
            return Single.just(preResolvedValues)
        }

        val variablesToResolve = requiredVariableIds.mapNotNull { variableMap[it] }
        return resolveVariables(variablesToResolve, preResolvedValues)
            .map {
                it.toMutableMap().also { resolvedVariables ->
                    resolvedVariables.forEach { resolvedVariable ->
                        resolvedVariables[resolvedVariable.key] = Variables.rawPlaceholdersToResolvedValues(resolvedVariable.value, resolvedVariables)
                    }
                }
            }
            .flatMap { resolvedVariables ->
                resolveRecursiveVariables(variableMap, resolvedVariables, recursionDepth + 1)
            }
    }

    private fun resolveVariables(variablesToResolve: List<Variable>, preResolvedValues: Map<String, String> = emptyMap()): Single<Map<String, String>> {
        var completable = Completable.complete()
        val resolvedVariables = mutableMapOf<String, String>()

        for (variable in variablesToResolve) {
            if (resolvedVariables.containsKey(variable.id)) {
                // Variable value is already resolved
                continue
            }
            if (preResolvedValues.containsKey(variable.key)) {
                // Variable value was pre-resolved
                resolvedVariables[variable.id] = preResolvedValues.getValue(variable.key)
                continue
            }

            val variableType = VariableTypeFactory.getType(variable.type)
            if (variableType is AsyncVariableType) {
                completable = completable.concatWith(
                    variableType.resolveValue(context, variable)
                        .doOnSuccess { resolvedValue ->
                            resolvedVariables[variable.id] = resolvedValue
                        }
                        .ignoreElement()
                )
            } else if (variableType is SyncVariableType) {
                resolvedVariables[variable.id] = variableType.resolveValue(variable)
            }
        }

        return completable
            .concatWith(resetVariableValues(variablesToResolve))
            .toSingle { resolvedVariables }
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
