package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import javax.inject.Inject

class VariableResolver
@Inject
constructor(
    private val variableTypeFactory: VariableTypeFactory,
) {
    suspend fun resolve(
        variableManager: VariableManager,
        requiredGlobalVariableIds: Set<GlobalVariableId>,
        dialogHandle: DialogHandle,
    ) {
        requiredGlobalVariableIds
            .filter { globalVariableId ->
                !variableManager.isResolved(globalVariableId)
            }
            .toSet()
            .let { globalVariableIds ->
                variableManager.globalVariables.filter { it.id in globalVariableIds }
            }
            .forEach { globalVariable ->
                resolveVariable(variableManager, globalVariable, dialogHandle)
            }
    }

    private suspend fun resolveVariable(
        variableManager: VariableManager,
        globalVariable: GlobalVariable,
        dialogHandle: DialogHandle,
        recursionDepth: Int = 0,
    ) {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return
        }
        if (variableManager.isResolved(globalVariable.id)) {
            return
        }

        val variableType = variableTypeFactory.getType(globalVariable.type)
        val rawValue = variableType.resolve(globalVariable, dialogHandle)

        Variables.extractGlobalVariableIds(rawValue)
            .forEach { variableId ->
                variableManager.getGlobalVariableById(variableId)
                    ?.let { referencedVariable ->
                        resolveVariable(variableManager, referencedVariable, dialogHandle, recursionDepth = recursionDepth + 1)
                    }
            }

        val finalValue = Variables.rawPlaceholdersToResolvedValues(
            rawValue,
            variableManager.getVariableValuesByIds(),
        )
        variableManager.setGlobalVariableValue(globalVariable, finalValue)
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        fun extractGlobalVariableIdsIncludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
            variableManager: VariableManager,
        ): Set<GlobalVariableId> =
            extractGlobalVariableIds(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                variableManager = variableManager,
                includeScripting = true,
            )

        fun extractGlobalVariableIdsExcludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
        ): Set<GlobalVariableId> =
            extractGlobalVariableIds(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                variableManager = null,
                includeScripting = false,
            )

        private fun extractGlobalVariableIds(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
            variableManager: VariableManager?,
            includeScripting: Boolean,
        ): Set<GlobalVariableId> =
            buildSet {
                addAll(Variables.extractGlobalVariableIds(shortcut.url))
                if (shortcut.authenticationType?.usesUsernameAndPassword == true) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.authUsername))
                    addAll(Variables.extractGlobalVariableIds(shortcut.authPassword))
                }
                if (shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.authToken))
                }
                if (shortcut.usesCustomBody() || shortcut.executionType == ShortcutExecutionType.MQTT) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.bodyContent))
                }
                if (shortcut.usesRequestParameters()) {
                    for (parameter in parameters) {
                        addAll(Variables.extractGlobalVariableIds(parameter.key))
                        addAll(Variables.extractGlobalVariableIds(parameter.value))
                    }
                }
                for (header in headers) {
                    addAll(Variables.extractGlobalVariableIds(header.key))
                    addAll(Variables.extractGlobalVariableIds(header.value))
                }

                if (shortcut.proxyHost != null) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.proxyHost))
                    if (shortcut.proxyType?.supportsAuthentication == true) {
                        shortcut.proxyUsername?.let { addAll(Variables.extractGlobalVariableIds(it)) }
                        shortcut.proxyPassword?.let { addAll(Variables.extractGlobalVariableIds(it)) }
                    }
                }

                if (includeScripting) {
                    addAll(extractGlobalVariableIdsFromJS(shortcut.codeOnPrepare, variableManager!!))
                    addAll(extractGlobalVariableIdsFromJS(shortcut.codeOnSuccess, variableManager))
                    addAll(extractGlobalVariableIdsFromJS(shortcut.codeOnFailure, variableManager))

                    addAll(Variables.extractGlobalVariableIds(shortcut.codeOnPrepare))
                    addAll(Variables.extractGlobalVariableIds(shortcut.codeOnSuccess))
                    addAll(Variables.extractGlobalVariableIds(shortcut.codeOnFailure))
                }

                if (shortcut.responseSuccessOutput == ResponseSuccessOutput.MESSAGE) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.responseSuccessMessage))
                }

                shortcut.responseStoreFileName?.let {
                    addAll(Variables.extractGlobalVariableIds(it))
                }

                if (shortcut.executionType == ShortcutExecutionType.WAKE_ON_LAN) {
                    addAll(Variables.extractGlobalVariableIds(shortcut.wolMacAddress))
                }
            }

        private fun extractGlobalVariableIdsFromJS(
            code: String,
            variableManager: VariableManager,
        ): Set<GlobalVariableId> =
            Variables.extractGlobalVariableIdsFromJS(code)
                .plus(
                    Variables.extractVariableKeysFromJS(code)
                        .map { variableKey ->
                            variableManager.getGlobalVariableByKey(variableKey)?.id ?: variableKey
                        },
                )
    }
}
