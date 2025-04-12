package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.import_export.models.ImportExportBase
import javax.inject.Inject

class ImportExportDefaultsProvider
@Inject
constructor() {
    fun applyDefaults(base: ImportExportBase): ImportExportBase {
        return base.copy(
            categories = base.categories?.map { category ->
                category.copy(
                    layoutType = category.layoutType ?: CategoryLayoutType.LINEAR_LIST.type,
                    background = category.background ?: CategoryBackgroundType.Default.serialize(),
                    scale = category.scale ?: 1f,
                    sections = category.sections ?: emptyList(),
                    shortcuts = category.shortcuts?.map { shortcut ->
                        shortcut.copy(
                            executionType = shortcut.executionType ?: ShortcutExecutionType.HTTP.type,
                            method = shortcut.method ?: HttpMethod.GET.method,
                            timeout = shortcut.timeout ?: 10_000,
                            launcherShortcut = shortcut.launcherShortcut != false,
                            requestBodyType = shortcut.requestBodyType ?: RequestBodyType.CUSTOM_TEXT.type,
                            responseHandling = shortcut.responseHandling?.let { responseHandling ->
                                responseHandling.copy(
                                    uiType = responseHandling.uiType ?: ResponseUiType.WINDOW.type,
                                    successOutput = responseHandling.successOutput ?: ResponseSuccessOutput.RESPONSE.type,
                                    successMessage = responseHandling.successMessage ?: "",
                                    failureOutput = responseHandling.failureOutput ?: ResponseFailureOutput.DETAILED.type,
                                    jsonArrayAsTable = responseHandling.jsonArrayAsTable != false,
                                )
                            },
                            url = shortcut.url ?: "",
                            bodyContent = shortcut.bodyContent ?: "",
                            codeOnPrepare = shortcut.codeOnPrepare ?: "",
                            codeOnSuccess = shortcut.codeOnSuccess ?: "",
                            codeOnFailure = shortcut.codeOnFailure ?: "",
                            followRedirects = shortcut.followRedirects != false,
                            acceptCookies = shortcut.acceptCookies != false,
                            wolPort = shortcut.wolPort ?: 9,
                            wolBroadcastAddress = shortcut.wolBroadcastAddress ?: "255.255.255.255",
                            username = shortcut.username ?: "",
                            password = shortcut.password ?: "",
                            authToken = shortcut.authToken ?: "",
                            headers = shortcut.headers ?: emptyList(),
                            parameters = shortcut.parameters?.map { parameter ->
                                parameter.copy(
                                    type = parameter.type ?: ParameterType.STRING.type,
                                )
                            }
                                ?: emptyList(),
                        )
                    }
                        ?: emptyList(),
                )
            }
                ?: emptyList(),
            variables = base.variables
                ?.map { variable ->
                    variable.copy(
                        type = variable.type ?: VariableType.CONSTANT.type,
                    )
                }
                ?: emptyList(),
            certificatePins = base.certificatePins ?: emptyList(),
            workingDirectories = base.workingDirectories ?: emptyList(),
        )
    }
}
