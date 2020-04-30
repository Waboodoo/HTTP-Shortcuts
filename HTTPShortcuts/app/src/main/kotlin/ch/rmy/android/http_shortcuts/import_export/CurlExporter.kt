package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.variables.Variables.rawPlaceholdersToResolvedValues
import ch.rmy.curlcommand.CurlCommand

object CurlExporter {

    fun generateCommand(shortcut: Shortcut, variableValues: Map<String, String>): CurlCommand =
        CurlCommand.Builder()
            .url(rawPlaceholdersToResolvedValues(shortcut.url, variableValues))
            .username(rawPlaceholdersToResolvedValues(shortcut.username, variableValues))
            .password(rawPlaceholdersToResolvedValues(shortcut.password, variableValues))
            .method(shortcut.method)
            .timeout(shortcut.timeout)
            .mapFor(shortcut.headers) { builder, header ->
                builder.header(
                    rawPlaceholdersToResolvedValues(header.key, variableValues),
                    rawPlaceholdersToResolvedValues(header.value, variableValues)
                )
            }
            .mapIf(shortcut.usesRequestParameters()) { builder ->
                if (shortcut.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA) {
                    builder
                        .isFormData()
                        .mapFor(shortcut.parameters) { builder2, parameter ->
                            if (parameter.isFileParameter || parameter.isFilesParameter) {
                                builder2.addFileParameter(
                                    rawPlaceholdersToResolvedValues(parameter.key, variableValues)
                                )
                            } else {
                                builder2.addParameter(
                                    rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                                    rawPlaceholdersToResolvedValues(parameter.value, variableValues)
                                )
                            }
                        }
                } else {
                    builder.mapFor(shortcut.parameters) { builder2, parameter ->
                        builder2.addParameter(
                            rawPlaceholdersToResolvedValues(parameter.key, variableValues),
                            rawPlaceholdersToResolvedValues(parameter.value, variableValues)
                        )
                    }
                }
            }
            .mapIf(shortcut.usesCustomBody()) { builder ->
                builder
                    .header(HttpHeaders.CONTENT_TYPE, shortcut.contentType.ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE })
                    .data(rawPlaceholdersToResolvedValues(shortcut.bodyContent, variableValues))
            }
            .build()

}