package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import javax.inject.Inject

class VariableResolver
@Inject
constructor(
    private val variableTypeFactory: VariableTypeFactory,
) {

    suspend fun resolve(
        variableManager: VariableManager,
        requiredVariableIds: Set<VariableId>,
        dialogHandle: DialogHandle,
    ): VariableManager {
        requiredVariableIds
            .filter { variableId ->
                !variableManager.isResolved(variableId)
            }
            .toSet()
            .let { variableIds ->
                variableManager.variables.filter { it.id in variableIds }
            }
            .forEach { variable ->
                resolveVariable(variableManager, variable, dialogHandle)
            }
        return variableManager
    }

    private suspend fun resolveVariable(
        variableManager: VariableManager,
        variable: Variable,
        dialogHandle: DialogHandle,
        recursionDepth: Int = 0,
    ) {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return
        }
        if (variableManager.isResolved(variable.id)) {
            return
        }

        val variableType = variableTypeFactory.getType(variable.variableType)
        val rawValue = variableType.resolve(variable, dialogHandle)

        Variables.extractVariableIds(rawValue)
            .forEach { variableId ->
                variableManager.getVariableById(variableId)
                    ?.let { referencedVariable ->
                        resolveVariable(variableManager, referencedVariable, dialogHandle, recursionDepth = recursionDepth + 1)
                    }
            }

        val finalValue = Variables.rawPlaceholdersToResolvedValues(
            rawValue,
            variableManager.getVariableValuesByIds(),
        )
        variableManager.setVariableValue(variable, finalValue)
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        fun extractVariableIdsIncludingScripting(shortcut: Shortcut, variableLookup: VariableLookup): Set<VariableId> =
            extractVariableIds(shortcut, variableLookup = variableLookup, includeScripting = true)

        fun extractVariableIdsExcludingScripting(shortcut: Shortcut): Set<VariableId> =
            extractVariableIds(shortcut, variableLookup = null, includeScripting = false)

        private fun extractVariableIds(shortcut: Shortcut, variableLookup: VariableLookup?, includeScripting: Boolean): Set<VariableId> =
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
                    if (shortcut.proxyType.supportsAuthentication) {
                        shortcut.proxyUsername?.let { addAll(Variables.extractVariableIds(it)) }
                        shortcut.proxyPassword?.let { addAll(Variables.extractVariableIds(it)) }
                    }
                }

                if (includeScripting) {
                    addAll(extractVariableIdsFromJS(shortcut.codeOnPrepare, variableLookup!!))
                    addAll(extractVariableIdsFromJS(shortcut.codeOnSuccess, variableLookup))
                    addAll(extractVariableIdsFromJS(shortcut.codeOnFailure, variableLookup))

                    addAll(Variables.extractVariableIds(shortcut.codeOnPrepare))
                    addAll(Variables.extractVariableIds(shortcut.codeOnSuccess))
                    addAll(Variables.extractVariableIds(shortcut.codeOnFailure))
                }

                if (shortcut.responseHandling != null && shortcut.responseHandling!!.successOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE) {
                    addAll(Variables.extractVariableIds(shortcut.responseHandling!!.successMessage))
                }

                shortcut.responseHandling?.storeFileName?.let {
                    addAll(Variables.extractVariableIds(it))
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
