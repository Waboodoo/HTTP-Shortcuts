package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import android.net.Uri
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.data.enums.SecurityPolicy
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.curlcommand.CurlCommand
import java.net.URLDecoder
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class TemporaryShortcutRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {

    fun observeTemporaryShortcut(): Flow<Shortcut> = queryFlow {
        shortcutDao().observeShortcutById(TEMPORARY_ID)
            .mapNotNull { it.firstOrNull() }
    }

    suspend fun createNewTemporaryShortcut(initialIcon: ShortcutIcon, executionType: ShortcutExecutionType, categoryId: CategoryId) = query {
        shortcutDao().insertOrUpdateShortcut(
            Shortcut(
                id = TEMPORARY_ID,
                icon = initialIcon,
                executionType = executionType,
                categoryId = categoryId,
                name = "",
                description = "",
                hidden = false,
                method = HttpMethod.GET,
                url = when (executionType) {
                    ShortcutExecutionType.HTTP,
                    ShortcutExecutionType.BROWSER,
                    -> "https://"
                    ShortcutExecutionType.MQTT -> "tcp://"
                    else -> ""
                },
                authenticationType = null,
                authUsername = "",
                authPassword = "",
                authToken = "",
                sectionId = null,
                bodyContent = "",
                timeout = 10_000,
                isWaitForNetwork = false,
                securityPolicy = null,
                launcherShortcut = true,
                secondaryLauncherShortcut = false,
                quickSettingsTileShortcut = false,
                delay = 0,
                repetitionInterval = null,
                contentType = "",
                fileUploadType = null,
                fileUploadSourceFile = null,
                fileUploadUseImageEditor = false,
                confirmationType = null,
                followRedirects = true,
                acceptCookies = true,
                keepConnectionOpen = false,
                wifiSsid = null,
                codeOnPrepare = "",
                codeOnSuccess = "",
                codeOnFailure = "",
                targetBrowser = TargetBrowser.Browser(packageName = null),
                excludeFromHistory = false,
                clientCertParams = null,
                requestBodyType = RequestBodyType.CUSTOM_TEXT,
                ipVersion = null,
                proxyType = null,
                proxyHost = null,
                proxyPort = null,
                proxyUsername = null,
                proxyPassword = null,
                excludeFromFileSharing = false,
                runInForegroundService = false,
                wolMacAddress = "",
                wolPort = if (executionType == ShortcutExecutionType.WAKE_ON_LAN) 9 else 0,
                wolBroadcastAddress = if (executionType == ShortcutExecutionType.WAKE_ON_LAN) "255.255.255.255" else "",
                responseActions = if (executionType == ShortcutExecutionType.HTTP) {
                    listOf(
                        ResponseDisplayAction.RERUN,
                        ResponseDisplayAction.SHARE,
                        ResponseDisplayAction.SAVE,
                    )
                        .joinToString(separator = ",") { it.key }
                } else {
                    ""
                },
                responseUiType = ResponseUiType.WINDOW,
                responseSuccessOutput = ResponseSuccessOutput.RESPONSE,
                responseFailureOutput = ResponseFailureOutput.DETAILED,
                responseContentType = null,
                responseCharset = null,
                responseSuccessMessage = "",
                responseIncludeMetaInfo = false,
                responseJsonArrayAsTable = true,
                responseMonospace = false,
                responseFontSize = null,
                responseJavaScriptEnabled = false,
                responseStoreDirectoryId = null,
                responseStoreFileName = null,
                responseReplaceFileIfExists = false,
                sortingOrder = -1,
            ),
        )
    }

    suspend fun getTemporaryShortcut(): Shortcut = query {
        shortcutDao().getShortcutById(TEMPORARY_ID).first()
    }

    suspend fun setIcon(icon: ShortcutIcon) {
        updateShortcut {
            copy(icon = icon)
        }
    }

    suspend fun setName(name: String) {
        updateShortcut {
            copy(name = name)
        }
    }

    suspend fun setDescription(description: String) {
        updateShortcut {
            copy(description = description)
        }
    }

    suspend fun setRepetitionInterval(interval: Duration? = null) {
        updateShortcut {
            copy(repetitionInterval = interval?.inWholeMinutes?.toInt())
        }
    }

    suspend fun setExcludeFromFileSharingChanged(exclude: Boolean) {
        updateShortcut {
            copy(excludeFromFileSharing = exclude)
        }
    }

    suspend fun setMethod(method: HttpMethod) {
        updateShortcut {
            copy(method = method)
        }
    }

    suspend fun setUrl(url: String) {
        updateShortcut {
            copy(url = url.trim())
        }
    }

    suspend fun setTargetBrowser(targetBrowser: TargetBrowser) {
        updateShortcut {
            copy(targetBrowser = targetBrowser)
        }
    }

    suspend fun setWolMacAddress(macAddress: String) {
        updateShortcut {
            copy(wolMacAddress = macAddress)
        }
    }

    suspend fun setWolPort(port: Int) {
        updateShortcut {
            copy(wolPort = port)
        }
    }

    suspend fun setWolBroadcastAddress(broadcastAddress: String) {
        updateShortcut {
            copy(wolBroadcastAddress = broadcastAddress)
        }
    }

    suspend fun setAuthenticationType(authenticationType: ShortcutAuthenticationType?) {
        updateShortcut {
            copy(authenticationType = authenticationType)
        }
    }

    suspend fun setUsername(username: String) {
        updateShortcut {
            copy(authUsername = username)
        }
    }

    suspend fun setPassword(password: String) {
        updateShortcut {
            copy(authPassword = password)
        }
    }

    suspend fun setToken(token: String) {
        updateShortcut {
            copy(authToken = token)
        }
    }

    suspend fun setRequestBodyType(type: RequestBodyType) {
        commitTransactionForShortcut { shortcut ->
            shortcutDao().insertOrUpdateShortcut(
                shortcut.copy(
                    requestBodyType = type,
                ),
            )

            if (type != RequestBodyType.FORM_DATA) {
                val requestParameterDao = requestParameterDao()
                requestParameterDao.getRequestParametersByShortcutId(TEMPORARY_ID)
                    .forEach { parameter ->
                        if (parameter.parameterType != ParameterType.STRING) {
                            requestParameterDao.insertOrUpdateRequestParameter(
                                parameter.copy(
                                    parameterType = ParameterType.STRING,
                                    fileUploadType = null,
                                    fileUploadFileName = null,
                                    fileUploadSourceFile = null,
                                    fileUploadUseImageEditor = false,
                                ),
                            )
                        }
                    }
            }
        }
    }

    suspend fun setContentType(contentType: String) {
        updateShortcut {
            copy(contentType = contentType.trim())
        }
    }

    suspend fun setBodyContent(bodyContent: String) {
        updateShortcut {
            copy(bodyContent = bodyContent)
        }
    }

    suspend fun setResponseUiType(responseUiType: ResponseUiType) {
        updateShortcut {
            copy(responseUiType = responseUiType)
        }
    }

    suspend fun setResponseContentType(responseContentType: ResponseContentType?) {
        updateShortcut {
            copy(responseContentType = responseContentType)
        }
    }

    suspend fun setCharsetOverride(charset: Charset?) {
        updateShortcut {
            copy(responseCharset = charset)
        }
    }

    suspend fun setResponseSuccessOutput(responseSuccessOutput: ResponseSuccessOutput) {
        updateShortcut {
            copy(responseSuccessOutput = responseSuccessOutput)
        }
    }

    suspend fun setResponseFailureOutput(responseFailureOutput: ResponseFailureOutput) {
        updateShortcut {
            copy(responseFailureOutput = responseFailureOutput)
        }
    }

    suspend fun setResponseSuccessMessage(responseSuccessMessage: String) {
        updateShortcut {
            copy(responseSuccessMessage = responseSuccessMessage)
        }
    }

    suspend fun setStoreFileName(fileName: String) {
        updateShortcut {
            copy(responseStoreFileName = fileName.takeUnlessEmpty())
        }
    }

    suspend fun setStoreDirectory(workingDirectoryId: WorkingDirectoryId?) {
        updateShortcut {
            copy(responseStoreDirectoryId = workingDirectoryId)
        }
    }

    suspend fun setStoreReplaceIfExists(enabled: Boolean) {
        updateShortcut {
            copy(responseReplaceFileIfExists = enabled)
        }
    }

    suspend fun setUseMonospaceFont(enabled: Boolean) {
        updateShortcut {
            copy(responseMonospace = enabled)
        }
    }

    suspend fun setFontSize(fontSize: Int?) {
        updateShortcut {
            copy(responseFontSize = fontSize)
        }
    }

    suspend fun setResponseIncludeMetaInfo(includeMetaInfo: Boolean) {
        updateShortcut {
            copy(responseIncludeMetaInfo = includeMetaInfo)
        }
    }

    suspend fun setDisplayActions(actions: List<ResponseDisplayAction>) {
        updateShortcut {
            copy(responseActions = actions.joinToString(",") { it.key })
        }
    }

    suspend fun setCode(onPrepare: String, onSuccess: String, onFailure: String) {
        updateShortcut {
            copy(
                codeOnPrepare = onPrepare,
                codeOnSuccess = onSuccess,
                codeOnFailure = onFailure,
            )
        }
    }

    suspend fun setWaitForConnection(waitForConnection: Boolean) {
        updateShortcut {
            copy(isWaitForNetwork = waitForConnection)
        }
    }

    suspend fun setExcludeFromHistory(excludeFromHistory: Boolean) {
        updateShortcut {
            copy(excludeFromHistory = excludeFromHistory)
        }
    }

    suspend fun setRunInForegroundService(runInForegroundService: Boolean) {
        updateShortcut {
            copy(runInForegroundService = runInForegroundService)
        }
    }

    suspend fun setConfirmationType(confirmationType: ConfirmationType?) {
        updateShortcut {
            copy(confirmationType = confirmationType)
        }
    }

    suspend fun setLauncherShortcut(launcherShortcut: Boolean) {
        updateShortcut {
            copy(launcherShortcut = launcherShortcut)
        }
    }

    suspend fun setSecondaryLauncherShortcut(secondaryLauncherShortcut: Boolean) {
        updateShortcut {
            copy(secondaryLauncherShortcut = secondaryLauncherShortcut)
        }
    }

    suspend fun setQuickSettingsTileShortcut(quickSettingsTileShortcut: Boolean) {
        updateShortcut {
            copy(quickSettingsTileShortcut = quickSettingsTileShortcut)
        }
    }

    suspend fun setDelay(delay: Duration) {
        updateShortcut {
            copy(delay = delay.inWholeMilliseconds.toInt())
        }
    }

    suspend fun setFollowRedirects(followRedirects: Boolean) {
        updateShortcut {
            copy(followRedirects = followRedirects)
        }
    }

    suspend fun setSecurityPolicy(securityPolicy: SecurityPolicy?) {
        updateShortcut {
            copy(securityPolicy = securityPolicy)
        }
    }

    suspend fun setAcceptCookies(acceptCookies: Boolean) {
        updateShortcut {
            copy(acceptCookies = acceptCookies)
        }
    }

    suspend fun setKeepConnectionOpen(keepConnectionOpen: Boolean) {
        updateShortcut {
            copy(keepConnectionOpen = keepConnectionOpen)
        }
    }

    suspend fun setTimeout(timeout: Duration) {
        updateShortcut {
            copy(timeout = timeout.inWholeMilliseconds.toInt())
        }
    }

    suspend fun setIpVersion(ipVersion: IpVersion?) {
        updateShortcut {
            copy(ipVersion = ipVersion)
        }
    }

    suspend fun setProxyType(proxyType: ProxyType) {
        updateShortcut {
            copy(proxyType = proxyType)
        }
    }

    suspend fun setProxyHost(host: String) {
        updateShortcut {
            copy(proxyHost = host.trim().takeUnlessEmpty())
        }
    }

    suspend fun setProxyPort(port: Int?) {
        updateShortcut {
            copy(proxyPort = port)
        }
    }

    suspend fun setProxyUsername(username: String) {
        updateShortcut {
            copy(proxyUsername = username.takeUnlessEmpty())
        }
    }

    suspend fun setProxyPassword(password: String) {
        updateShortcut {
            copy(proxyPassword = password.takeUnlessEmpty())
        }
    }

    suspend fun setWifiSsid(ssid: String) {
        updateShortcut {
            copy(wifiSsid = ssid.trim())
        }
    }

    suspend fun setClientCertParams(clientCertParams: ClientCertParams?) {
        updateShortcut {
            copy(clientCertParams = clientCertParams)
        }
    }

    suspend fun setUseImageEditor(useImageEditor: Boolean) {
        updateShortcut {
            copy(fileUploadUseImageEditor = useImageEditor)
        }
    }

    suspend fun setFileUploadType(fileUploadType: FileUploadType) {
        updateShortcut {
            copy(fileUploadType = fileUploadType)
        }
    }

    suspend fun setFileUploadUri(fileUploadUri: Uri?) {
        updateShortcut {
            copy(
                fileUploadSourceFile = fileUploadUri?.toString(),
                fileUploadType = if (fileUploadUri != null) {
                    FileUploadType.FILE
                } else {
                    fileUploadType
                },
            )
        }
    }

    suspend fun setJsonArrayAsTable(jsonArrayAsTable: Boolean) {
        updateShortcut {
            copy(responseJsonArrayAsTable = jsonArrayAsTable)
        }
    }

    suspend fun setJavaScriptEnabled(javaScriptEnabled: Boolean) {
        updateShortcut {
            copy(responseJavaScriptEnabled = javaScriptEnabled)
        }
    }

    suspend fun importFromCurl(curlCommand: CurlCommand) {
        commitTransactionForShortcut { shortcut ->
            val requestBodyType = if (curlCommand.usesBinaryData) {
                RequestBodyType.FILE
            } else if (curlCommand.isFormData || curlCommand.data.all { data -> data.count { it == '=' } == 1 }) {
                if (curlCommand.isFormData) {
                    RequestBodyType.FORM_DATA
                } else {
                    RequestBodyType.X_WWW_FORM_URLENCODE
                }
            } else {
                RequestBodyType.CUSTOM_TEXT
            }

            val usesProxy = curlCommand.proxyHost.isNotEmpty() && curlCommand.proxyPort != 0

            val newShortcut = shortcut.copy(
                method = HttpMethod.parse(curlCommand.method) ?: HttpMethod.GET,
                url = curlCommand.url,
                authUsername = curlCommand.username,
                authPassword = curlCommand.password,
                authenticationType = if (curlCommand.username.isNotEmpty() || curlCommand.password.isNotEmpty()) {
                    if (curlCommand.isDigestAuth) {
                        ShortcutAuthenticationType.DIGEST
                    } else {
                        ShortcutAuthenticationType.BASIC
                    }
                } else {
                    null
                },
                timeout = curlCommand.timeout.coerceIn(5000, 10 * 60 * 1000),
                ipVersion = when {
                    curlCommand.ipVersion4 -> IpVersion.V4
                    curlCommand.ipVersion6 -> IpVersion.V6
                    else -> null
                },
                requestBodyType = requestBodyType,
                bodyContent = if (requestBodyType == RequestBodyType.CUSTOM_TEXT) {
                    curlCommand.data.joinToString(separator = "&")
                } else {
                    ""
                },
                contentType = curlCommand.headers.getCaseInsensitive(HttpHeaders.CONTENT_TYPE) ?: "",
                proxyType = if (usesProxy) ProxyType.HTTP else null,
                proxyHost = if (usesProxy) curlCommand.proxyHost else null,
                proxyPort = if (usesProxy) curlCommand.proxyPort else null,
                securityPolicy = if (curlCommand.insecure) SecurityPolicy.AcceptAll else null,
                responseSuccessOutput = if (curlCommand.silent) ResponseSuccessOutput.NONE else shortcut.responseSuccessOutput,
                responseFailureOutput = if (curlCommand.silent) ResponseFailureOutput.NONE else shortcut.responseFailureOutput,
            )

            shortcutDao().insertOrUpdateShortcut(newShortcut)

            val requestHeaderDao = requestHeaderDao()
            var headerSortingOrder = 0
            curlCommand.headers.forEach { (key, value) ->
                if (!key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                    requestHeaderDao.insertOrUpdateRequestHeader(
                        RequestHeader(
                            shortcutId = TEMPORARY_ID,
                            key = key.filter { Validation.isValidInHeaderName(it) },
                            value = value.filter { Validation.isValidInHeaderValue(it) },
                            sortingOrder = headerSortingOrder,
                        ),
                    )
                    headerSortingOrder++
                }
            }

            if (requestBodyType == RequestBodyType.FORM_DATA || requestBodyType == RequestBodyType.X_WWW_FORM_URLENCODE) {
                val requestParameterDao = requestParameterDao()
                var parameterSortingOrder = 0
                curlCommand.data.forEach { potentialParameter ->
                    potentialParameter.split("=")
                        .takeIf { it.size == 2 }
                        ?.let { parameterParts ->
                            val key = parameterParts[0]
                            val value = parameterParts[1]
                            val isFileParameter = value.startsWith("@") && curlCommand.isFormData
                            val parameter = RequestParameter(
                                key = decode(key),
                                value = if (!isFileParameter) decode(value) else "",
                                shortcutId = TEMPORARY_ID,
                                parameterType = if (isFileParameter) ParameterType.FILE else ParameterType.STRING,
                                fileUploadType = null,
                                fileUploadFileName = null,
                                fileUploadSourceFile = null,
                                fileUploadUseImageEditor = false,
                                sortingOrder = parameterSortingOrder,
                            )
                            requestParameterDao.insertOrUpdateRequestParameter(parameter)
                            parameterSortingOrder++
                        }
                }
            }
        }
    }

    private suspend fun commitTransactionForShortcut(transaction: suspend Database.(Shortcut) -> Unit) {
        commitTransaction {
            transaction(
                shortcutDao().getShortcutById(TEMPORARY_ID)
                    .firstOrNull()
                    ?: return@commitTransaction,
            )
        }
    }

    private suspend fun updateShortcut(transformation: Shortcut.() -> Shortcut) {
        commitTransactionForShortcut { shortcut ->
            shortcutDao().insertOrUpdateShortcut(shortcut.transformation())
        }
    }

    suspend fun deleteTemporaryShortcut() {
        commitTransaction {
            shortcutDao().deleteShortcutById(TEMPORARY_ID)
            requestHeaderDao().deleteRequestHeaderByShortcutId(TEMPORARY_ID)
            requestParameterDao().deleteRequestParametersByShortcutId(TEMPORARY_ID)
        }
    }

    companion object {
        internal fun decode(text: String): String =
            try {
                URLDecoder.decode(text, "utf-8")
            } catch (e: IllegalArgumentException) {
                text
            }
    }
}
