package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
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

        val variableType = variableTypeFactory.getType(variable.type)
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

        fun extractVariableIdsIncludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
            variableLookup: VariableLookup,
        ): Set<VariableId> =
            extractVariableIds(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                variableLookup = variableLookup,
                includeScripting = true,
            )

        fun extractVariableIdsExcludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
        ): Set<VariableId> =
            extractVariableIds(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                variableLookup = null,
                includeScripting = false,
            )

        private fun extractVariableIds(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
            variableLookup: VariableLookup?,
            includeScripting: Boolean,
        ): Set<VariableId> =
            buildSet {
                addAll(Variables.extractVariableIds(shortcut.url))
                if (shortcut.authenticationType?.usesUsernameAndPassword == true) {
                    addAll(Variables.extractVariableIds(shortcut.authUsername))
                    addAll(Variables.extractVariableIds(shortcut.authPassword))
                }
                if (shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                    addAll(Variables.extractVariableIds(shortcut.authToken))
                }
                if (shortcut.usesCustomBody() || shortcut.executionType == ShortcutExecutionType.MQTT) {
                    addAll(Variables.extractVariableIds(shortcut.bodyContent))
                }
                if (shortcut.usesRequestParameters()) {
                    for (parameter in parameters) {
                        addAll(Variables.extractVariableIds(parameter.key))
                        addAll(Variables.extractVariableIds(parameter.value))
                    }
                }
                for (header in headers) {
                    addAll(Variables.extractVariableIds(header.key))
                    addAll(Variables.extractVariableIds(header.value))
                }

                if (shortcut.proxyHost != null) {
                    addAll(Variables.extractVariableIds(shortcut.proxyHost))
                    if (shortcut.proxyType?.supportsAuthentication == true) {
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

                if (shortcut.responseSuccessOutput == ResponseSuccessOutput.MESSAGE) {
                    addAll(Variables.extractVariableIds(shortcut.responseSuccessMessage))
                }

                shortcut.responseStoreFileName?.let {
                    addAll(Variables.extractVariableIds(it))
                }

                if (shortcut.executionType == ShortcutExecutionType.WAKE_ON_LAN) {
                    addAll(Variables.extractVariableIds(shortcut.wolMacAddress))
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
                        },
                )
    }
}
