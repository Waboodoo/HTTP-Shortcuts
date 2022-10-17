package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import javax.inject.Inject

class VariableResolver
@Inject
constructor(
    private val context: Context,
) {

    suspend fun resolve(
        variables: List<VariableModel>,
        shortcut: ShortcutModel,
        preResolvedValues: Map<VariableKey, String> = emptyMap(),
    ): VariableManager {
        val variableManager = VariableManager(variables)
        val requiredVariableIds = extractVariableIds(shortcut, variableManager, includeScripting = false)
            .toMutableSet()
        return resolve(variableManager, requiredVariableIds, preResolvedValues)
    }

    suspend fun resolve(
        variables: List<VariableModel>,
        requiredVariableIds: Set<VariableId>,
        preResolvedValues: Map<VariableKey, String> = emptyMap(),
    ): VariableManager {
        val variableManager = VariableManager(variables)
        return resolve(variableManager, requiredVariableIds, preResolvedValues)
    }

    private suspend fun resolve(
        variableManager: VariableManager,
        requiredVariableIds: Set<VariableId>,
        preResolvedValues: Map<VariableKey, String>,
    ): VariableManager {
        val preResolvedVariables = mutableMapOf<VariableModel, String>()
        preResolvedValues.forEach { (variableKey, value) ->
            variableManager.getVariableByKeyOrId(variableKey)?.let { variable ->
                preResolvedVariables[variable] = value
            }
        }

        val variablesToResolve = requiredVariableIds
            .mapNotNull { variableId ->
                variableManager.getVariableById(variableId)
            }

        val resolvedVariables = resolveVariables(variablesToResolve, preResolvedVariables)
        val resolvedValues = resolveRecursiveVariables(variableManager, resolvedVariables)
        resolvedValues.forEach { (variable, value) ->
            variableManager.setVariableValue(variable, value)
        }

        return variableManager
    }

    private suspend fun resolveRecursiveVariables(
        variableLookup: VariableLookup,
        preResolvedValues: Map<VariableModel, String>,
        recursionDepth: Int = 0,
    ): Map<VariableModel, String> {
        val requiredVariableIds = mutableSetOf<String>()
        preResolvedValues.values.forEach { value ->
            requiredVariableIds.addAll(Variables.extractVariableIds(value))
        }
        if (recursionDepth >= MAX_RECURSION_DEPTH || requiredVariableIds.isEmpty()) {
            return preResolvedValues
        }

        val variablesToResolve = requiredVariableIds
            .mapNotNull { variableId ->
                variableLookup.getVariableById(variableId)
            }
        return resolveVariables(variablesToResolve, preResolvedValues)
            .toMutableMap()
            .also { resolvedVariables ->
                resolvedVariables.forEach { resolvedVariable ->
                    resolvedVariables[resolvedVariable.key] =
                        Variables.rawPlaceholdersToResolvedValues(
                            resolvedVariable.value,
                            resolvedVariables.mapKeys { (variable, _) -> variable.id },
                        )
                }
            }
            .let { resolvedVariables ->
                resolveRecursiveVariables(variableLookup, resolvedVariables, recursionDepth + 1)
            }
    }

    suspend fun resolveVariables(
        variablesToResolve: List<VariableModel>,
        preResolvedValues: Map<VariableModel, String> = emptyMap(),
    ): Map<VariableModel, String> {
        val resolvedVariables = preResolvedValues.toMutableMap()

        for (variable in variablesToResolve) {
            if (resolvedVariables.keys.any { it.id == variable.id }) {
                // Variable value is already resolved
                continue
            }
            val preResolvedValue = preResolvedValues.entries
                .firstOrNull { it.key.id == variable.id }
                ?.value
            if (preResolvedValue != null) {
                // Variable value was pre-resolved
                resolvedVariables[variable] = preResolvedValue
                continue
            }

            val variableType = VariableTypeFactory.getType(variable.variableType)
            resolvedVariables[variable] = variableType.resolve(context, variable)
        }

        return resolvedVariables
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        fun extractVariableIds(shortcut: ShortcutModel, variableLookup: VariableLookup, includeScripting: Boolean = true): Set<VariableId> =
            buildSet {
                addAll(Variables.extractVariableIds(shortcut.url))
                if (shortcut.authenticationType.usesUsernameAndPassword) {
                    addAll(Variables.extractVariableIds(shortcut.username))
                    addAll(Variables.extractVariableIds(shortcut.password))
                }
                if (shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                    addAll(Variables.extractVariableIds(shortcut.authToken))
                }
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

                if (shortcut.proxyHost != null) {
                    addAll(Variables.extractVariableIds(shortcut.proxyHost!!))
                }

                if (includeScripting) {
                    addAll(extractVariableIdsFromJS(shortcut.codeOnPrepare, variableLookup))
                    addAll(extractVariableIdsFromJS(shortcut.codeOnSuccess, variableLookup))
                    addAll(extractVariableIdsFromJS(shortcut.codeOnFailure, variableLookup))

                    addAll(Variables.extractVariableIds(shortcut.codeOnPrepare))
                    addAll(Variables.extractVariableIds(shortcut.codeOnSuccess))
                    addAll(Variables.extractVariableIds(shortcut.codeOnFailure))
                }

                if (shortcut.responseHandling != null && shortcut.responseHandling!!.successOutput == ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE) {
                    addAll(Variables.extractVariableIds(shortcut.responseHandling!!.successMessage))
                }
            }

        private fun extractVariableIdsFromJS(
            code: String,
            variableLookup: VariableLookup,
        ): Set<VariableId> =
            Variables.extractVariableIdsFromJS(code)
                .plus(
                    Variables.extractVariableKeysFromJS(code)
                        .map { variableKey ->
                            variableLookup.getVariableByKey(variableKey)?.id ?: variableKey
                        }
                )
    }
}
