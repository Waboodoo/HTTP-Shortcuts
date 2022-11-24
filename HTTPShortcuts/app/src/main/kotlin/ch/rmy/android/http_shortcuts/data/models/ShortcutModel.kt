package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Shortcut")
open class ShortcutModel(
    @PrimaryKey
    var id: ShortcutId = "",
    icon: ShortcutIcon = ShortcutIcon.NoIcon,
    var executionType: String? = ShortcutExecutionType.APP.type,
) : RealmModel {

    @Required
    var name: String = ""

    private var iconName: String? = icon.toString().takeUnlessEmpty()

    @Required
    var method = METHOD_GET

    @Required
    var url: String = "https://"

    @Required
    var username: String = ""

    @Required
    var password: String = ""

    @Required
    var authToken: String = ""

    @Required
    var description: String = ""

    @Required
    var bodyContent: String = ""

    var timeout: Int = 10000

    @Required
    private var retryPolicy: String = RETRY_POLICY_NONE

    var headers: RealmList<HeaderModel> = RealmList()

    var parameters: RealmList<ParameterModel> = RealmList()

    var acceptAllCertificates: Boolean = false

    private var authentication: String? = ShortcutAuthenticationType.NONE.type

    var launcherShortcut: Boolean = false

    var quickSettingsTileShortcut: Boolean = false

    var delay: Int = 0

    @Required
    private var requestBodyType: String = RequestBodyType.CUSTOM_TEXT.type

    @Required
    var contentType: String = ""

    var responseHandling: ResponseHandlingModel? = null

    var requireConfirmation: Boolean = false

    var followRedirects: Boolean = true

    var acceptCookies: Boolean = true

    var proxyHost: String? = null

    var proxyPort: Int? = null

    var wifiSsid: String = ""

    @Required
    var clientCert: String = ""

    @Required
    var codeOnPrepare: String = ""

    @Required
    var codeOnSuccess: String = ""

    @Required
    var codeOnFailure: String = ""

    @Required
    var browserPackageName: String = ""

    var icon: ShortcutIcon
        get() = ShortcutIcon.fromName(iconName)
        set(value) {
            iconName = value.toString().takeUnlessEmpty()
        }

    var clientCertParams: ClientCertParams?
        get() = ClientCertParams.fromString(clientCert)
        set(value) {
            clientCert = value?.toString() ?: ""
        }

    var bodyType: RequestBodyType
        get() = RequestBodyType.parse(requestBodyType)
        set(value) {
            requestBodyType = value.type
        }

    var authenticationType: ShortcutAuthenticationType
        get() = ShortcutAuthenticationType.parse(authentication)
        set(value) {
            authentication = value.type
        }

    init {
        if (executionType == ShortcutExecutionType.APP.type) {
            responseHandling = ResponseHandlingModel()
        }
    }

    fun allowsBody(): Boolean =
        METHOD_POST == method ||
            METHOD_PUT == method ||
            METHOD_DELETE == method ||
            METHOD_PATCH == method ||
            METHOD_OPTIONS == method

    fun usesRequestParameters() =
        allowsBody() &&
            (bodyType.let { it == RequestBodyType.FORM_DATA || it == RequestBodyType.X_WWW_FORM_URLENCODE })

    fun usesCustomBody() = allowsBody() && bodyType == RequestBodyType.CUSTOM_TEXT

    fun usesGenericFileBody() =
        allowsBody() && bodyType == RequestBodyType.FILE

    fun usesImageFileBody() =
        allowsBody() && bodyType == RequestBodyType.IMAGE

    fun isSameAs(other: ShortcutModel): Boolean {
        if (other.name != name ||
            other.bodyContent != bodyContent ||
            other.description != description ||
            other.iconName != iconName ||
            other.method != method ||
            other.password != password ||
            other.authToken != authToken ||
            other.retryPolicy != retryPolicy ||
            other.timeout != timeout ||
            other.url != url ||
            other.username != username ||
            other.authentication != authentication ||
            other.launcherShortcut != launcherShortcut ||
            other.quickSettingsTileShortcut != quickSettingsTileShortcut ||
            other.acceptAllCertificates != acceptAllCertificates ||
            other.delay != delay ||
            other.parameters.size != parameters.size ||
            other.headers.size != headers.size ||
            other.requestBodyType != requestBodyType ||
            other.contentType != contentType ||
            other.codeOnPrepare != codeOnPrepare ||
            other.codeOnSuccess != codeOnSuccess ||
            other.codeOnFailure != codeOnFailure ||
            other.followRedirects != followRedirects ||
            other.requireConfirmation != requireConfirmation ||
            other.acceptCookies != acceptCookies ||
            other.proxyHost != proxyHost ||
            other.proxyPort != proxyPort ||
            other.wifiSsid != wifiSsid ||
            other.clientCert != clientCert ||
            other.browserPackageName != browserPackageName
        ) {
            return false
        }
        if (other.parameters.indices.any { !parameters[it]!!.isSameAs(other.parameters[it]!!) }) {
            return false
        }
        if (other.headers.indices.any { !headers[it]!!.isSameAs(other.headers[it]!!) }) {
            return false
        }
        if ((other.responseHandling == null) != (responseHandling == null)) {
            return false
        }
        if (other.responseHandling?.isSameAs(responseHandling!!) == false) {
            return false
        }
        return true
    }

    var isWaitForNetwork
        get() = retryPolicy == RETRY_POLICY_WAIT_FOR_INTERNET
        set(value) {
            retryPolicy = if (value) RETRY_POLICY_WAIT_FOR_INTERNET else RETRY_POLICY_NONE
        }

    val usesResponseBody: Boolean
        get() = type.usesResponse &&
            (
                (
                    responseHandling?.successOutput == ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE ||
                        responseHandling?.failureOutput == ResponseHandlingModel.FAILURE_OUTPUT_DETAILED
                    ) ||
                    codeOnSuccess.isNotEmpty() || codeOnFailure.isNotEmpty()
                )

    fun validate() {
        require(id.isUUID() || id.isInt()) {
            "Invalid shortcut ID found, must be UUID: $id"
        }
        require(name.length <= NAME_MAX_LENGTH) {
            "Shortcut name too long: $name"
        }
        require(name.isNotBlank()) {
            "Shortcut must have a name"
        }
        require(method in setOf(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_PATCH, METHOD_DELETE, METHOD_HEAD, METHOD_OPTIONS, METHOD_TRACE)) {
            "Invalid method: $method"
        }
        require(ShortcutExecutionType.values().any { it.type == executionType }) {
            "Invalid shortcut executionType: $executionType"
        }
        require(retryPolicy in setOf(RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET)) {
            "Invalid retry policy: $retryPolicy"
        }
        require(RequestBodyType.values().any { it.type == requestBodyType }) {
            "Invalid request body type: $requestBodyType"
        }
        require(ShortcutAuthenticationType.values().any { it.type == authentication }) {
            "Invalid authentication: $authentication"
        }
        require(timeout >= 0) {
            "Invalid timeout: $timeout"
        }
        require(delay >= 0) {
            "Invalid delay: $delay"
        }
        headers.forEach(HeaderModel::validate)
        parameters.forEach(ParameterModel::validate)
        responseHandling?.validate()
    }

    companion object {

        const val TEMPORARY_ID: ShortcutId = "0"
        const val NAME_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 200

        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"

        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"
        const val METHOD_PATCH = "PATCH"
        const val METHOD_HEAD = "HEAD"
        const val METHOD_OPTIONS = "OPTIONS"
        const val METHOD_TRACE = "TRACE"

        private const val RETRY_POLICY_NONE = "none"
        private const val RETRY_POLICY_WAIT_FOR_INTERNET = "wait_for_internet"

        const val DEFAULT_CONTENT_TYPE = "text/plain"
    }
}
