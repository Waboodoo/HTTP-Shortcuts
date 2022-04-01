package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import ch.rmy.android.framework.extensions.detachFromRealm
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables.rawPlaceholdersToResolvedValues
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Single

class CurlExporter(val context: Context) {

    fun generateCommand(shortcut: ShortcutModel): Single<CurlCommand> {
        val detachedShortcut = shortcut.detachFromRealm()
        return resolveVariables(detachedShortcut)
            .map { variableManager ->
                generateCommand(detachedShortcut, variableManager.getVariableValuesByIds())
            }
    }

    private fun resolveVariables(shortcut: ShortcutModel) =
        VariableRepository().getVariables()
            .flatMap { variables ->
                VariableResolver(context)
                    .resolve(variables, shortcut)
            }

    private fun generateCommand(shortcut: ShortcutModel, variableValues: Map<VariableKey, String>): CurlCommand =
        CurlCommand.Builder()
            .url(rawPlaceholdersToResolvedValues(shortcut.url, variableValues))
            .mapIf(shortcut.authenticationType.usesUsernameAndPassword) {
                username(rawPlaceholdersToResolvedValues(shortcut.username, variableValues))
                    .password(rawPlaceholdersToResolvedValues(shortcut.password, variableValues))
            }
            .mapIf(shortcut.authenticationType == ShortcutAuthenticationType.BEARER) {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${shortcut.authToken}")
            }
            .mapIf(!shortcut.proxyHost.isNullOrEmpty() && shortcut.proxyPort != null) {
                proxy(shortcut.proxyHost!!, shortcut.proxyPort!!)
            }
            .method(shortcut.method)
            .mapIf(shortcut.timeout != 10000) {
                timeout(shortcut.timeout)
            }
            .mapFor(shortcut.headers) { header ->
                header(
                    rawPlaceholdersToResolvedValues(header.key, variableValues),
                    rawPlaceholdersToResolvedValues(header.value, variableValues)
                )
            }
            .mapIf(shortcut.usesFileBody()) {
                usesBinaryData()
            }
            .mapIf(shortcut.usesRequestParameters()) {
                if (shortcut.bodyType == RequestBodyType.FORM_DATA) {
                    isFormData()
                        .mapFor(shortcut.parameters) { parameter ->
                            if (parameter.isFileParameter || parameter.isFilesParameter) {
                                addFileParameter(
                                    rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                                )
                            } else {
                                addParameter(
                                    rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                                    rawPlaceholdersToResolvedValues(parameter.value, variableValues),
                                )
                            }
                        }
                } else {
                    mapFor(shortcut.parameters) { parameter ->
                        addParameter(
                            rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                            rawPlaceholdersToResolvedValues(parameter.value, variableValues),
                        )
                    }
                }
            }
            .mapIf(shortcut.usesCustomBody()) {
                header(HttpHeaders.CONTENT_TYPE, shortcut.contentType.ifEmpty { ShortcutModel.DEFAULT_CONTENT_TYPE })
                    .data(rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues))
            }
            .build()
}
