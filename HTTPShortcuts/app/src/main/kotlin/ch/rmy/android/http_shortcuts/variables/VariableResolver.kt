package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.getGlobalVariables
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import javax.inject.Inject

class VariableResolver
@Inject
constructor(
    private val variableTypeFactory: VariableTypeFactory,
) {
    suspend fun resolve(
        variableManager: VariableManager,
        variableKeysOrIds: Set<VariableKeyOrId>,
        dialogHandle: DialogHandle,
    ) {
        val globalVariableIds = variableKeysOrIds.getGlobalVariables(variableManager)
        variableManager.globalVariables
            .filter { it.id in globalVariableIds }
            .forEach { globalVariable ->
                resolveGlobalVariable(variableManager, globalVariable, dialogHandle)
            }
    }

    private suspend fun resolveGlobalVariable(
        variableManager: VariableManager,
        globalVariable: GlobalVariable,
        dialogHandle: DialogHandle,
        recursionDepth: Int = 0,
    ) {
        if (recursionDepth >= MAX_RECURSION_DEPTH) {
            return
        }
        if (variableManager.isResolved(globalVariable)) {
            return
        }

        val variableType = variableTypeFactory.getType(globalVariable.type)
        val rawValue = variableType.resolve(globalVariable, dialogHandle)

        Variables.findResolvableVariableIdentifiers(rawValue)
            .getGlobalVariables(variableManager)
            .forEach { variableId ->
                variableManager.getGlobalVariableById(variableId)
                    ?.let { referencedVariable ->
                        resolveGlobalVariable(variableManager, referencedVariable, dialogHandle, recursionDepth = recursionDepth + 1)
                    }
            }

        val finalValue = Variables.rawPlaceholdersToResolvedValues(
            rawValue,
            variableManager.getVariableValues(),
        )
        variableManager.setGlobalVariableValue(globalVariable, finalValue)
    }

    companion object {

        private const val MAX_RECURSION_DEPTH = 3

        fun findResolvableVariableIdentifiersIncludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
        ): Set<VariableKeyOrId> =
            findResolvableVariableIdentifiers(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                includeScripting = true,
            )

        fun findResolvableVariableIdentifiersExcludingScripting(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
        ): Set<VariableKeyOrId> =
            findResolvableVariableIdentifiers(
                shortcut = shortcut,
                headers = headers,
                parameters = parameters,
                includeScripting = false,
            )

        private fun findResolvableVariableIdentifiers(
            shortcut: Shortcut,
            headers: List<RequestHeader>,
            parameters: List<RequestParameter>,
            includeScripting: Boolean,
        ): Set<VariableKeyOrId> =
            buildSet {
                addAll(Variables.findResolvableVariableIdentifiers(shortcut.url))
                if (shortcut.authenticationType?.usesUsernameAndPassword == true) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.authUsername))
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.authPassword))
                }
                if (shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.authToken))
                }
                if (shortcut.usesCustomBody() || shortcut.executionType == ShortcutExecutionType.MQTT) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.bodyContent))
                }
                if (shortcut.usesRequestParameters()) {
                    for (parameter in parameters) {
                        addAll(Variables.findResolvableVariableIdentifiers(parameter.key))
                        addAll(Variables.findResolvableVariableIdentifiers(parameter.value))
                    }
                }
                for (header in headers) {
                    addAll(Variables.findResolvableVariableIdentifiers(header.key))
                    addAll(Variables.findResolvableVariableIdentifiers(header.value))
                }

                if (shortcut.proxyHost != null) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.proxyHost))
                    if (shortcut.proxyType?.supportsAuthentication == true) {
                        shortcut.proxyUsername?.let { addAll(Variables.findResolvableVariableIdentifiers(it)) }
                        shortcut.proxyPassword?.let { addAll(Variables.findResolvableVariableIdentifiers(it)) }
                    }
                }

                if (includeScripting) {
                    addAll(Variables.findResolvableVariableIdentifiersFromJS(shortcut.codeOnPrepare))
                    addAll(Variables.findResolvableVariableIdentifiersFromJS(shortcut.codeOnSuccess))
                    addAll(Variables.findResolvableVariableIdentifiersFromJS(shortcut.codeOnFailure))

                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.codeOnPrepare))
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.codeOnSuccess))
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.codeOnFailure))
                }

                if (shortcut.responseSuccessOutput == ResponseSuccessOutput.MESSAGE) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.responseSuccessMessage))
                }

                shortcut.responseStoreFileName?.let {
                    addAll(Variables.findResolvableVariableIdentifiers(it))
                }

                if (shortcut.executionType == ShortcutExecutionType.WAKE_ON_LAN) {
                    addAll(Variables.findResolvableVariableIdentifiers(shortcut.wolMacAddress))
                }
            }
    }
}
