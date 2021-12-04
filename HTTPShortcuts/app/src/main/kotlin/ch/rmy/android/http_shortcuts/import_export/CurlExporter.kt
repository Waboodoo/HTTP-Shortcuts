package ch.rmy.android.http_shortcuts.import_export

import android.content.Context
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables.rawPlaceholdersToResolvedValues
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Single

object CurlExporter {

    fun generateCommand(context: Context, shortcut: Shortcut): Single<CurlCommand> {
        val detachedShortcut = shortcut.detachFromRealm()
        return resolveVariables(context, detachedShortcut)
            .map { variableManager ->
                generateCommand(detachedShortcut, variableManager.getVariableValuesByIds())
            }
    }

    private fun resolveVariables(context: Context, shortcut: Shortcut) =
        Controller().use { controller ->
            VariableResolver(context)
                .resolve(controller.getVariables().detachFromRealm(), shortcut)
        }

    private fun generateCommand(shortcut: Shortcut, variableValues: Map<String, String>): CurlCommand =
        CurlCommand.Builder()
            .url(rawPlaceholdersToResolvedValues(shortcut.url, variableValues))
            .mapIf(shortcut.usesBasicAuthentication() || shortcut.usesDigestAuthentication()) {
                username(rawPlaceholdersToResolvedValues(shortcut.username, variableValues))
                    .password(rawPlaceholdersToResolvedValues(shortcut.password, variableValues))
            }
            .mapIf(shortcut.usesBearerAuthentication()) {
                header(HttpHeaders.AUTHORIZATION, "Bearer ${shortcut.authToken}")
            }
            .mapIf(!shortcut.proxyHost.isNullOrEmpty() && shortcut.proxyPort != null) {
                proxy(shortcut.proxyHost!!, shortcut.proxyPort!!)
            }
            .method(shortcut.method)
            .timeout(shortcut.timeout)
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
                if (shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA) {
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
                header(HttpHeaders.CONTENT_TYPE, shortcut.contentType.ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE })
                    .data(rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues))
            }
            .build()
}
