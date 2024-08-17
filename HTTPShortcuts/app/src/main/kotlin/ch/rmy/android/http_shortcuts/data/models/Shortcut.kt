package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Shortcut() : RealmObject {

    constructor(
        id: ShortcutId = "",
        icon: ShortcutIcon = ShortcutIcon.NoIcon,
        executionType: ShortcutExecutionType = ShortcutExecutionType.APP,
        categoryId: CategoryId? = null
    ) : this() {
        this.id = id
        this.icon = icon
        this.executionType = executionType.type
        if (executionType == ShortcutExecutionType.APP) {
            responseHandling = ResponseHandling()
        }
        this.categoryId = categoryId
    }

    @PrimaryKey
    var id: ShortcutId = ""
    var executionType: String? = ShortcutExecutionType.APP.type

    // Only valid when id == TEMPORARY_ID
    var categoryId: CategoryId? = null

    var name: String = ""

    private var iconName: String? = icon.toString().takeUnlessEmpty()

    var hidden: Boolean = false

    var method = METHOD_GET

    var url: String = "https://"

    var username: String = ""

    var password: String = ""

    var authToken: String = ""

    var description: String = ""

    var bodyContent: String = ""

    var timeout: Int = 10000

    private var retryPolicy: String = RETRY_POLICY_NONE

    var headers: RealmList<Header> = realmListOf()

    var parameters: RealmList<Parameter> = realmListOf()

    var acceptAllCertificates: Boolean = false

    /**
     * Hex-encoded SHA-1 or SHA-256 fingerprint of expected (self-signed) server certificate, or empty string if not used
     */
    var certificateFingerprint: String = ""

    private var authentication: String? = ShortcutAuthenticationType.NONE.type

    var launcherShortcut: Boolean = true

    var secondaryLauncherShortcut: Boolean = false

    var quickSettingsTileShortcut: Boolean = false

    var delay: Int = 0

    private var requestBodyType: String = RequestBodyType.CUSTOM_TEXT.type

    var contentType: String = ""

    var responseHandling: ResponseHandling? = null

    var fileUploadOptions: FileUploadOptions? = null

    private var confirmation: String? = null

    var confirmationType: ConfirmationType?
        get() = ConfirmationType.parse(confirmation)
        set(value) {
            confirmation = value?.type
        }

    var followRedirects: Boolean = true

    var acceptCookies: Boolean = true

    var keepConnectionOpen: Boolean = false

    private var proxy: String = "HTTP"

    var proxyHost: String? = null

    var proxyPort: Int? = null

    var proxyUsername: String? = null

    var proxyPassword: String? = null

    var wifiSsid: String = ""

    var clientCert: String = ""

    var codeOnPrepare: String = ""

    var codeOnSuccess: String = ""

    var codeOnFailure: String = ""

    private var browserPackageName: String = ""

    var targetBrowser: TargetBrowser
        get() = TargetBrowser.parse(browserPackageName)
        set(value) {
            browserPackageName = value.serialize()
        }

    var excludeFromHistory: Boolean = false

    var repetition: Repetition? = null

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

    var proxyType: ProxyType
        get() = ProxyType.parse(proxy)
        set(value) {
            proxy = value.type
        }

    var excludeFromFileSharing: Boolean = false

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

    fun isSameAs(other: Shortcut): Boolean {
        if (other.name != name ||
            other.bodyContent != bodyContent ||
            other.description != description ||
            other.iconName != iconName ||
            other.hidden != hidden ||
            other.method != method ||
            other.password != password ||
            other.authToken != authToken ||
            other.retryPolicy != retryPolicy ||
            other.timeout != timeout ||
            other.url != url ||
            other.username != username ||
            other.authentication != authentication ||
            other.launcherShortcut != launcherShortcut ||
            other.secondaryLauncherShortcut != secondaryLauncherShortcut ||
            other.quickSettingsTileShortcut != quickSettingsTileShortcut ||
            other.acceptAllCertificates != acceptAllCertificates ||
            other.certificateFingerprint != certificateFingerprint ||
            other.delay != delay ||
            other.parameters.size != parameters.size ||
            other.headers.size != headers.size ||
            other.requestBodyType != requestBodyType ||
            other.contentType != contentType ||
            other.codeOnPrepare != codeOnPrepare ||
            other.codeOnSuccess != codeOnSuccess ||
            other.codeOnFailure != codeOnFailure ||
            other.followRedirects != followRedirects ||
            other.confirmation != confirmation ||
            other.acceptCookies != acceptCookies ||
            other.keepConnectionOpen != keepConnectionOpen ||
            other.proxyType != proxyType ||
            other.proxyHost != proxyHost ||
            other.proxyPort != proxyPort ||
            other.proxyUsername != proxyUsername ||
            other.proxyPassword != proxyPassword ||
            other.wifiSsid != wifiSsid ||
            other.clientCert != clientCert ||
            other.browserPackageName != browserPackageName ||
            other.excludeFromHistory != excludeFromHistory ||
            other.excludeFromFileSharing != excludeFromFileSharing
        ) {
            return false
        }
        if (other.parameters.indices.any { !parameters[it].isSameAs(other.parameters[it]) }) {
            return false
        }
        if (other.headers.indices.any { !headers[it].isSameAs(other.headers[it]) }) {
            return false
        }
        if ((other.responseHandling == null) != (responseHandling == null)) {
            return false
        }
        if (other.responseHandling?.isSameAs(responseHandling!!) == false) {
            return false
        }
        if ((other.fileUploadOptions == null) != (fileUploadOptions == null)) {
            return false
        }
        if (other.fileUploadOptions?.isSameAs(fileUploadOptions!!) == false) {
            return false
        }
        if ((other.repetition == null) != (repetition == null)) {
            return false
        }
        if (other.repetition?.isSameAs(repetition!!) == false) {
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
                    responseHandling?.successOutput == ResponseHandling.SUCCESS_OUTPUT_RESPONSE ||
                        responseHandling?.failureOutput == ResponseHandling.FAILURE_OUTPUT_DETAILED
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
        require(ShortcutExecutionType.entries.any { it.type == executionType }) {
            "Invalid shortcut executionType: $executionType"
        }
        require(retryPolicy in setOf(RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET)) {
            "Invalid retry policy: $retryPolicy"
        }
        require(RequestBodyType.entries.any { it.type == requestBodyType }) {
            "Invalid request body type: $requestBodyType"
        }
        require(ShortcutAuthenticationType.entries.any { it.type == authentication }) {
            "Invalid authentication: $authentication"
        }
        require(timeout >= 0) {
            "Invalid timeout: $timeout"
        }
        require(delay >= 0) {
            "Invalid delay: $delay"
        }
        require(certificateFingerprint.isEmpty() || certificateFingerprint.isValidCertificateFingerprint()) {
            "Invalid self-signed certificate fingerprint found: $certificateFingerprint"
        }
        headers.forEach(Header::validate)
        parameters.forEach(Parameter::validate)
        responseHandling?.validate()
        repetition?.validate()
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
