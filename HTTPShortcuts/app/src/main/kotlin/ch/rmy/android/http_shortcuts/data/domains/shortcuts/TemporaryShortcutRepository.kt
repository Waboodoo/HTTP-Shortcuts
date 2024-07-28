package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import android.net.Uri
import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmFactory
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ResponseContentType
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.FileUploadOptions
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Repetition
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.curlcommand.CurlCommand
import kotlinx.coroutines.flow.Flow
import java.net.URLDecoder
import java.nio.charset.Charset
import javax.inject.Inject
import kotlin.time.Duration

class TemporaryShortcutRepository
@Inject
constructor(
    realmFactory: RealmFactory,
) : BaseRepository(realmFactory) {

    fun getObservableTemporaryShortcut(): Flow<Shortcut> =
        observeItem {
            getTemporaryShortcut()
        }

    suspend fun createNewTemporaryShortcut(initialIcon: ShortcutIcon, executionType: ShortcutExecutionType, categoryId: CategoryId) {
        commitTransaction {
            copyOrUpdate(
                Shortcut(
                    id = Shortcut.TEMPORARY_ID,
                    icon = initialIcon,
                    executionType = executionType,
                    categoryId = categoryId,
                )
            )
        }
    }

    suspend fun getTemporaryShortcut(): Shortcut =
        queryItem {
            getTemporaryShortcut()
        }

    suspend fun setIcon(icon: ShortcutIcon) {
        commitTransactionForShortcut { shortcut ->
            shortcut.icon = icon
        }
    }

    suspend fun setName(name: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.name = name
        }
    }

    suspend fun setDescription(description: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.description = description
        }
    }

    suspend fun setRepetitionInterval(interval: Duration? = null) {
        commitTransactionForShortcut { shortcut ->
            if (interval == null) {
                shortcut.repetition?.delete()
                shortcut.repetition = null
            } else {
                if (shortcut.repetition == null) {
                    shortcut.repetition = Repetition(interval = interval.inWholeMinutes.toInt())
                } else {
                    shortcut.repetition?.interval = interval.inWholeMinutes.toInt()
                }
            }
        }
    }

    suspend fun setExcludeFromFileSharingChanged(exclude: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.excludeFromFileSharing = exclude
        }
    }

    suspend fun setMethod(method: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.method = method
        }
    }

    suspend fun setUrl(url: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.url = url.trim()
        }
    }

    suspend fun setTargetBrowser(targetBrowser: TargetBrowser) {
        commitTransactionForShortcut { shortcut ->
            shortcut.targetBrowser = targetBrowser
        }
    }

    suspend fun setAuthenticationType(authenticationType: ShortcutAuthenticationType) {
        commitTransactionForShortcut { shortcut ->
            shortcut.authenticationType = authenticationType
        }
    }

    suspend fun setUsername(username: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.username = username
        }
    }

    suspend fun setPassword(password: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.password = password
        }
    }

    suspend fun setToken(token: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.authToken = token
        }
    }

    suspend fun moveHeader(headerId1: String, headerId2: String) =
        commitTransactionForShortcut { shortcut ->
            shortcut.headers.swap(headerId1, headerId2) { id }
        }

    suspend fun addHeader(key: String, value: String): Header {
        val header = Header(
            key = key.trim(),
            value = value,
        )
        commitTransactionForShortcut { shortcut ->
            shortcut.headers.add(
                copy(header)
            )
        }
        return header
    }

    suspend fun updateHeader(headerId: String, key: String, value: String) {
        commitTransactionForShortcut { shortcut ->
            val header = shortcut.headers
                .find { it.id == headerId }
                ?: return@commitTransactionForShortcut
            header.key = key.trim()
            header.value = value
        }
    }

    suspend fun removeHeader(headerId: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.headers
                .find { it.id == headerId }
                ?.delete()
        }
    }

    suspend fun setRequestBodyType(type: RequestBodyType) {
        commitTransactionForShortcut { shortcut ->
            shortcut.bodyType = type
            if (type != RequestBodyType.FORM_DATA) {
                shortcut.parameters
                    .filterNot { it.isStringParameter }
                    .forEach { parameter ->
                        parameter.delete()
                    }
            }
        }
    }

    suspend fun moveParameter(parameterId1: String, parameterId2: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.parameters.swap(parameterId1, parameterId2) { id }
        }
    }

    suspend fun addParameter(type: ParameterType, key: String, value: String, fileName: String, fileUploadOptions: FileUploadOptions): Parameter {
        val parameter = Parameter(
            parameterType = type,
            key = key.trim(),
            value = value,
            fileName = fileName,
            fileUploadOptions = fileUploadOptions,
        )
        commitTransactionForShortcut { shortcut ->
            shortcut.parameters.add(
                copy(parameter)
            )
        }
        return parameter
    }

    suspend fun updateParameter(parameterId: String, key: String, value: String = "", fileName: String = "", fileUploadOptions: FileUploadOptions) {
        commitTransactionForShortcut { shortcut ->
            val parameter = shortcut.parameters
                .find { it.id == parameterId }
                ?: return@commitTransactionForShortcut
            parameter.key = key.trim()
            parameter.value = value
            parameter.fileName = fileName
            parameter.fileUploadOptions = fileUploadOptions
        }
    }

    suspend fun removeParameter(parameterId: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.parameters
                .find { it.id == parameterId }
                ?.delete()
        }
    }

    suspend fun setContentType(contentType: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.contentType = contentType.trim()
        }
    }

    suspend fun setBodyContent(bodyContent: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.bodyContent = bodyContent
        }
    }

    suspend fun setResponseUiType(responseUiType: String) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.uiType = responseUiType
        }
    }

    suspend fun setResponseContentType(responseContentType: ResponseContentType?) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.responseContentType = responseContentType
        }
    }

    suspend fun setCharsetOverride(charset: Charset?) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.charsetOverride = charset
        }
    }

    suspend fun setResponseSuccessOutput(responseSuccessOutput: String) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.successOutput = responseSuccessOutput
        }
    }

    suspend fun setResponseFailureOutput(responseFailureOutput: String) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.failureOutput = responseFailureOutput
        }
    }

    suspend fun setResponseSuccessMessage(responseSuccessMessage: String) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.successMessage = responseSuccessMessage
        }
    }

    suspend fun setStoreFileName(fileName: String) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.storeFileName = fileName.takeUnlessEmpty()
        }
    }

    suspend fun setStoreDirectory(workingDirectoryId: WorkingDirectoryId?) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.storeDirectoryId = workingDirectoryId
        }
    }

    suspend fun setStoreReplaceIfExists(enabled: Boolean) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.replaceFileIfExists = enabled
        }
    }

    suspend fun setUseMonospaceFont(enabled: Boolean) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.monospace = enabled
        }
    }

    suspend fun setFontSize(fontSize: Int?) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.fontSize = fontSize
        }
    }

    suspend fun setResponseIncludeMetaInfo(includeMetaInfo: Boolean) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.includeMetaInfo = includeMetaInfo
        }
    }

    suspend fun setDisplayActions(actions: List<ResponseDisplayAction>) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.displayActions = actions
        }
    }

    suspend fun setCode(onPrepare: String, onSuccess: String, onFailure: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.codeOnPrepare = onPrepare
            shortcut.codeOnSuccess = onSuccess
            shortcut.codeOnFailure = onFailure
        }
    }

    suspend fun setWaitForConnection(waitForConnection: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.isWaitForNetwork = waitForConnection
        }
    }

    suspend fun setExcludeFromHistory(excludeFromHistory: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.excludeFromHistory = excludeFromHistory
        }
    }

    suspend fun setConfirmationType(confirmationType: ConfirmationType?) {
        commitTransactionForShortcut { shortcut ->
            shortcut.confirmationType = confirmationType
        }
    }

    suspend fun setLauncherShortcut(launcherShortcut: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.launcherShortcut = launcherShortcut
        }
    }

    suspend fun setSecondaryLauncherShortcut(secondaryLauncherShortcut: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.secondaryLauncherShortcut = secondaryLauncherShortcut
        }
    }

    suspend fun setQuickSettingsTileShortcut(quickSettingsTileShortcut: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.quickSettingsTileShortcut = quickSettingsTileShortcut
        }
    }

    suspend fun setDelay(delay: Duration) {
        commitTransactionForShortcut { shortcut ->
            shortcut.delay = delay.inWholeMilliseconds.toInt()
        }
    }

    suspend fun setFollowRedirects(followRedirects: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.followRedirects = followRedirects
        }
    }

    suspend fun setAcceptAllCertificates(acceptAllCertificates: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.acceptAllCertificates = acceptAllCertificates
        }
    }

    suspend fun setCertificateFingerprint(certificateFingerprint: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.certificateFingerprint = certificateFingerprint
        }
    }

    suspend fun setAcceptCookies(acceptCookies: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.acceptCookies = acceptCookies
        }
    }

    suspend fun setKeepConnectionOpen(keepConnectionOpen: Boolean) {
        commitTransactionForShortcut { shortcut ->
            shortcut.keepConnectionOpen = keepConnectionOpen
        }
    }

    suspend fun setTimeout(timeout: Duration) {
        commitTransactionForShortcut { shortcut ->
            shortcut.timeout = timeout.inWholeMilliseconds.toInt()
        }
    }

    suspend fun setProxyType(proxyType: ProxyType) {
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyType = proxyType
        }
    }

    suspend fun setProxyHost(host: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyHost = host.trim().takeUnless { it.isEmpty() }
        }
    }

    suspend fun setProxyPort(port: Int?) {
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyPort = port
        }
    }

    suspend fun setProxyUsername(username: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyUsername = username.takeUnless { it.isEmpty() }
        }
    }

    suspend fun setProxyPassword(password: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyPassword = password.takeUnless { it.isEmpty() }
        }
    }

    suspend fun setWifiSsid(ssid: String) {
        commitTransactionForShortcut { shortcut ->
            shortcut.wifiSsid = ssid.trim()
        }
    }

    suspend fun setClientCertParams(clientCertParams: ClientCertParams?) {
        commitTransactionForShortcut { shortcut ->
            shortcut.clientCertParams = clientCertParams
        }
    }

    suspend fun setUseImageEditor(useImageEditor: Boolean) {
        commitTransactionForFileUploadOptions { fileUploadOptions ->
            fileUploadOptions.useImageEditor = useImageEditor
        }
    }

    suspend fun setFileUploadType(fileUploadType: FileUploadType) {
        commitTransactionForFileUploadOptions { fileUploadOptions ->
            fileUploadOptions.type = fileUploadType
        }
    }

    suspend fun setFileUploadUri(fileUploadUri: Uri?) {
        commitTransactionForFileUploadOptions { fileUploadOptions ->
            fileUploadOptions.file = fileUploadUri?.toString()
            if (fileUploadUri != null) {
                fileUploadOptions.type = FileUploadType.FILE
            }
        }
    }

    suspend fun setJsonArrayAsTable(jsonArrayAsTable: Boolean) {
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.jsonArrayAsTable = jsonArrayAsTable
        }
    }

    suspend fun importFromCurl(curlCommand: CurlCommand) {
        commitTransactionForShortcut { shortcut ->
            shortcut.method = curlCommand.method
            shortcut.url = curlCommand.url
            shortcut.username = curlCommand.username
            shortcut.password = curlCommand.password
            if (curlCommand.username.isNotEmpty() || curlCommand.password.isNotEmpty()) {
                shortcut.authenticationType = if (curlCommand.isDigestAuth) {
                    ShortcutAuthenticationType.DIGEST
                } else {
                    ShortcutAuthenticationType.BASIC
                }
            }
            shortcut.timeout = curlCommand.timeout

            if (curlCommand.usesBinaryData) {
                shortcut.bodyType = RequestBodyType.FILE
            } else if (curlCommand.isFormData || curlCommand.data.all { data -> data.count { it == '=' } == 1 }) {
                shortcut.bodyType = if (curlCommand.isFormData) {
                    RequestBodyType.FORM_DATA
                } else {
                    RequestBodyType.X_WWW_FORM_URLENCODE
                }
                prepareParameters(curlCommand, shortcut)
            } else {
                shortcut.bodyContent = curlCommand.data.joinToString(separator = "&")
                shortcut.bodyType = RequestBodyType.CUSTOM_TEXT
            }
            curlCommand.headers.getCaseInsensitive(HttpHeaders.CONTENT_TYPE)
                ?.let {
                    shortcut.contentType = it
                }
            curlCommand.headers.forEach { (key, value) ->
                if (!key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                    shortcut.headers.add(copy(Header(key = key, value = value)))
                }
            }

            if (curlCommand.proxyHost.isNotEmpty() && curlCommand.proxyPort != 0) {
                shortcut.proxyType = ProxyType.HTTP
                shortcut.proxyHost = curlCommand.proxyHost
                shortcut.proxyPort = curlCommand.proxyPort
            }

            if (curlCommand.insecure) {
                shortcut.acceptAllCertificates = true
            }

            if (curlCommand.silent) {
                shortcut.responseHandling?.successOutput = ResponseHandling.SUCCESS_OUTPUT_NONE
                shortcut.responseHandling?.failureOutput = ResponseHandling.FAILURE_OUTPUT_NONE
            }
        }
    }

    private fun RealmTransactionContext.prepareParameters(curlCommand: CurlCommand, shortcut: Shortcut) {
        curlCommand.data.forEach { potentialParameter ->
            potentialParameter.split("=")
                .takeIf { it.size == 2 }
                ?.let { parameterParts ->
                    val key = parameterParts[0]
                    val value = parameterParts[1]
                    val parameter = if (value.startsWith("@") && curlCommand.isFormData) {
                        Parameter(
                            key = decode(key),
                            parameterType = ParameterType.FILE,
                        )
                    } else {
                        Parameter(
                            key = decode(key),
                            value = decode(value),
                        )
                    }
                    shortcut.parameters.add(copy(parameter))
                }
        }
    }

    private suspend fun commitTransactionForShortcut(transaction: RealmTransactionContext.(Shortcut) -> Unit) {
        commitTransaction {
            transaction(
                getTemporaryShortcut()
                    .findFirst()
                    ?: return@commitTransaction
            )
        }
    }

    private suspend fun commitTransactionForResponseHandling(
        transaction: RealmTransactionContext.(ResponseHandling) -> Unit,
    ) {
        commitTransactionForShortcut { shortcut ->
            shortcut.responseHandling
                ?.let { responseHandling ->
                    transaction(responseHandling)
                }
        }
    }

    private suspend fun commitTransactionForFileUploadOptions(
        transaction: RealmTransactionContext.(FileUploadOptions) -> Unit,
    ) {
        commitTransactionForShortcut { shortcut ->
            if (shortcut.fileUploadOptions == null) {
                shortcut.fileUploadOptions = FileUploadOptions()
            }
            transaction(shortcut.fileUploadOptions!!)
        }
    }

    suspend fun deleteTemporaryShortcut() {
        commitTransactionForShortcut { shortcut ->
            shortcut.delete()
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
