package ch.rmy.android.http_shortcuts.data.domains.shortcuts

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.data.RealmTransactionContext
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.framework.extensions.swap
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.getTemporaryShortcut
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.net.URLDecoder
import kotlin.time.Duration

class TemporaryShortcutRepository : BaseRepository(RealmFactory.getInstance()) {

    fun getObservableTemporaryShortcut(): Observable<Shortcut> =
        observeItem {
            getTemporaryShortcut()
        }

    fun createNewTemporaryShortcut(initialIcon: ShortcutIcon, executionType: ShortcutExecutionType): Completable =
        commitTransaction {
            copyOrUpdate(
                Shortcut(
                    id = Shortcut.TEMPORARY_ID,
                    icon = initialIcon,
                    executionType = executionType.type,
                    responseHandling = if (executionType == ShortcutExecutionType.APP) {
                        ResponseHandling()
                    } else null,
                )
            )
        }

    fun getTemporaryShortcut(): Single<Shortcut> =
        queryItem {
            getTemporaryShortcut()
        }

    fun setIcon(icon: ShortcutIcon): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.icon = icon
        }

    fun setName(name: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.name = name
        }

    fun setDescription(description: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.description = description
        }

    fun setMethod(method: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.method = method
        }

    fun setUrl(url: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.url = url.trim()
        }

    fun setBrowserPackageName(packageName: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.browserPackageName = packageName.trim()
        }

    fun setAuthenticationMethod(authenticationMethod: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.authentication = authenticationMethod
        }

    fun setUsername(username: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.username = username
        }

    fun setPassword(password: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.password = password
        }

    fun setToken(token: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.authToken = token
        }

    fun moveHeader(headerId1: String, headerId2: String) =
        commitTransactionForShortcut { shortcut ->
            shortcut.headers.swap(headerId1, headerId2) { id }
        }

    fun addHeader(key: String, value: String) =
        Single.defer {
            val header = Header(
                key = key.trim(),
                value = value,
            )
            commitTransactionForShortcut { shortcut ->
                shortcut.headers.add(
                    copy(header)
                )
            }
                .toSingleDefault(header)
        }

    fun updateHeader(headerId: String, key: String, value: String) =
        commitTransactionForShortcut { shortcut ->
            val header = shortcut.headers
                .find { it.id == headerId }
                ?: return@commitTransactionForShortcut
            header.key = key.trim()
            header.value = value
        }

    fun removeHeader(headerId: String) =
        commitTransactionForShortcut { shortcut ->
            shortcut.headers
                .find { it.id == headerId }
                ?.deleteFromRealm()
        }

    fun setRequestBodyType(type: RequestBodyType): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.bodyType = type
            if (type != RequestBodyType.FORM_DATA) {
                shortcut.parameters
                    .filterNot { it.isStringParameter }
                    .forEach { parameter ->
                        parameter.deleteFromRealm()
                    }
            }
        }

    fun moveParameter(parameterId1: String, parameterId2: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.parameters.swap(parameterId1, parameterId2) { id }
        }

    fun addStringParameter(key: String, value: String) =
        Single.defer {
            val parameter = Parameter(
                type = Parameter.TYPE_STRING,
                key = key.trim(),
                value = value,
            )
            commitTransactionForShortcut { shortcut ->
                shortcut.parameters.add(
                    copy(parameter)
                )
            }
                .toSingleDefault(parameter)
        }

    fun addFileParameter(key: String, fileName: String, multiple: Boolean) =
        Single.defer {
            val parameter = Parameter(
                type = if (multiple) Parameter.TYPE_FILES else Parameter.TYPE_FILE,
                key = key.trim(),
                fileName = fileName,
            )
            commitTransactionForShortcut { shortcut ->
                shortcut.parameters.add(
                    copy(parameter)
                )
            }
                .toSingleDefault(parameter)
        }

    fun updateParameter(parameterId: String, key: String, value: String = "", fileName: String = "") =
        commitTransactionForShortcut { shortcut ->
            val parameter = shortcut.parameters
                .find { it.id == parameterId }
                ?: return@commitTransactionForShortcut
            parameter.key = key.trim()
            parameter.value = value
            parameter.fileName = fileName
        }

    fun removeParameter(parameterId: String) =
        commitTransactionForShortcut { shortcut ->
            shortcut.parameters
                .find { it.id == parameterId }
                ?.deleteFromRealm()
        }

    fun setContentType(contentType: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.contentType = contentType.trim()
        }

    fun setBodyContent(bodyContent: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.bodyContent = bodyContent
        }

    fun setResponseUiType(responseUiType: String): Completable =
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.uiType = responseUiType
        }

    fun setResponseSuccessOutput(responseSuccessOutput: String): Completable =
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.successOutput = responseSuccessOutput
        }

    fun setResponseFailureOutput(responseFailureOutput: String): Completable =
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.failureOutput = responseFailureOutput
        }

    fun setResponseSuccessMessage(responseSuccessMessage: String): Completable =
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.successMessage = responseSuccessMessage
        }

    fun setResponseIncludeMetaInfo(includeMetaInfo: Boolean): Completable =
        commitTransactionForResponseHandling { responseHandling ->
            responseHandling.includeMetaInfo = includeMetaInfo
        }

    fun setCodeOnPrepare(code: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.codeOnPrepare = code.trim()
        }

    fun setCodeOnSuccess(code: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.codeOnSuccess = code.trim()
        }

    fun setCodeOnFailure(code: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.codeOnFailure = code.trim()
        }

    fun setWaitForConnection(waitForConnection: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.isWaitForNetwork = waitForConnection
        }

    fun setRequireConfirmation(requireConfirmation: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.requireConfirmation = requireConfirmation
        }

    fun setLauncherShortcut(launcherShortcut: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.launcherShortcut = launcherShortcut
        }

    fun setQuickSettingsTileShortcut(quickSettingsTileShortcut: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.quickSettingsTileShortcut = quickSettingsTileShortcut
        }

    fun setDelay(delay: Duration): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.delay = delay.inWholeMilliseconds.toInt()
        }

    fun setFollowRedirects(followRedirects: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.followRedirects = followRedirects
        }

    fun setAcceptAllCertificates(acceptAllCertificates: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.acceptAllCertificates = acceptAllCertificates
        }

    fun setAcceptCookies(acceptCookies: Boolean): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.acceptCookies = acceptCookies
        }

    fun setTimeout(timeout: Duration): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.timeout = timeout.inWholeMilliseconds.toInt()
        }

    fun setProxyHost(host: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyHost = host.trim().takeUnless { it.isEmpty() }
        }

    fun setProxyPort(port: Int?): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.proxyPort = port
        }

    fun setWifiSsid(ssid: String): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.wifiSsid = ssid.trim()
        }

    fun setClientCertParams(clientCertParams: ClientCertParams?): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.clientCertParams = clientCertParams
        }

    fun importFromCurl(curlCommand: CurlCommand) =
        commitTransactionForShortcut { shortcut ->
            shortcut.method = curlCommand.method
            shortcut.url = curlCommand.url
            shortcut.username = curlCommand.username
            shortcut.password = curlCommand.password
            if (curlCommand.username.isNotEmpty() || curlCommand.password.isNotEmpty()) {
                shortcut.authentication = Shortcut.AUTHENTICATION_BASIC
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
                            type = Parameter.TYPE_FILE,
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

    private fun commitTransactionForShortcut(transaction: RealmTransactionContext.(Shortcut) -> Unit) =
        commitTransaction {
            transaction(
                getTemporaryShortcut()
                    .findFirst()
                    ?: return@commitTransaction
            )
        }

    private fun commitTransactionForResponseHandling(
        transaction: RealmTransactionContext.(ResponseHandling) -> Unit,
    ): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.responseHandling
                ?.let { responseHandling ->
                    transaction(responseHandling)
                }
        }

    fun deleteTemporaryShortcut(): Completable =
        commitTransactionForShortcut { shortcut ->
            shortcut.deleteFromRealm()
        }

    companion object {
        private fun decode(text: String): String =
            try {
                URLDecoder.decode(text, "utf-8")
            } catch (e: IllegalArgumentException) {
                text
            }
    }
}
