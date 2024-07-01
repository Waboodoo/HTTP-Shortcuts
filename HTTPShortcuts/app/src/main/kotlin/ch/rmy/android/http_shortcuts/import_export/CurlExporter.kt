package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.framework.extensions.runFor
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.resolve
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables.rawPlaceholdersToResolvedValues
import ch.rmy.curlcommand.CurlCommand
import javax.inject.Inject

class CurlExporter
@Inject
constructor(
    private val variableRepository: VariableRepository,
    private val variableResolver: VariableResolver,
) {

    suspend fun generateCommand(shortcut: Shortcut, dialogHandle: DialogHandle): CurlCommand {
        val variableManager = resolveVariables(shortcut, dialogHandle)
        return generateCommand(shortcut, variableManager.getVariableValuesByIds())
    }

    private suspend fun resolveVariables(shortcut: Shortcut, dialogHandle: DialogHandle): VariableManager {
        val variables = variableRepository.getVariables()
        return variableResolver.resolve(VariableManager(variables), shortcut, dialogHandle)
    }

    private fun generateCommand(shortcut: Shortcut, variableValues: Map<VariableKey, String>): CurlCommand =
        CurlCommand.Builder()
            .url(rawPlaceholdersToResolvedValues(shortcut.url, variableValues))
            .runIf(shortcut.authenticationType.usesUsernameAndPassword) {
                username(rawPlaceholdersToResolvedValues(shortcut.username, variableValues))
                    .password(rawPlaceholdersToResolvedValues(shortcut.password, variableValues))
                    .runIf(shortcut.authenticationType == ShortcutAuthenticationType.DIGEST) {
                        isDigestAuth()
                    }
            }
            .runIf(shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${shortcut.authToken}")
            }
            .runIf(!shortcut.proxyHost.isNullOrEmpty() && shortcut.proxyPort != null) {
                proxy(shortcut.proxyHost!!, shortcut.proxyPort!!)
            }
            .method(shortcut.method)
            .runIf(shortcut.timeout != 10000) {
                timeout(shortcut.timeout)
            }
            .runFor(shortcut.headers) { header ->
                header(
                    rawPlaceholdersToResolvedValues(header.key, variableValues),
                    rawPlaceholdersToResolvedValues(header.value, variableValues)
                )
            }
            .runIf(shortcut.usesGenericFileBody()) {
                usesBinaryData()
            }
            .runIf(shortcut.usesRequestParameters()) {
                if (shortcut.bodyType == RequestBodyType.FORM_DATA) {
                    isFormData()
                        .runFor(shortcut.parameters) { parameter ->
                            when (parameter.parameterType) {
                                ParameterType.FILE,
                                -> {
                                    addFileParameter(
                                        rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                                    )
                                }
                                ParameterType.STRING -> {
                                    addParameter(
                                        rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                                        rawPlaceholdersToResolvedValues(parameter.value, variableValues),
                                    )
                                }
                            }
                        }
                } else {
                    runFor(shortcut.parameters) { parameter ->
                        addParameter(
                            rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                            rawPlaceholdersToResolvedValues(parameter.value, variableValues),
                        )
                    }
                }
            }
            .runIf(shortcut.usesCustomBody()) {
                header(HttpHeaders.CONTENT_TYPE, shortcut.contentType.ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE })
                    .data(rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues))
            }
            .runIf(shortcut.acceptAllCertificates) {
                insecure()
            }
            .runIf(
                shortcut.responseHandling?.run {
                    successOutput == ResponseHandling.SUCCESS_OUTPUT_NONE && failureOutput == ResponseHandling.FAILURE_OUTPUT_NONE
                } == true
            ) {
                silent()
            }
            .build()
}
