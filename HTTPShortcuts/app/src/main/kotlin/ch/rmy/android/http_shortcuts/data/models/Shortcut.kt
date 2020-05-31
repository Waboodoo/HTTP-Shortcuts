package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.type
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Shortcut(
    @PrimaryKey
    override var id: String = "",
    var iconName: String? = null,
    var executionType: String? = ShortcutExecutionType.APP.type
) : RealmObject(), HasId {

    @Required
    var name: String = ""

    @Required
    var method = METHOD_GET

    @Required
    var url: String = "http://"

    @Required
    var username: String = ""

    @Required
    var password: String = ""

    @Required
    var authToken: String = ""

    @Required
    var feedback: String = FEEDBACK_TOAST

    @Required
    var description: String = ""

    @Required
    var bodyContent: String = ""

    var timeout: Int = 10000

    @Required
    var retryPolicy: String = RETRY_POLICY_NONE

    var headers: RealmList<Header> = RealmList()

    var parameters: RealmList<Parameter> = RealmList()

    var acceptAllCertificates: Boolean = false

    var authentication: String? = AUTHENTICATION_NONE

    var launcherShortcut: Boolean = false

    var quickSettingsTileShortcut: Boolean = false

    var delay: Int = 0

    @Required
    var requestBodyType: String = REQUEST_BODY_TYPE_CUSTOM_TEXT

    @Required
    var contentType: String = ""

    var requireConfirmation: Boolean = false

    var followRedirects: Boolean = true

    var proxyHost: String? = null

    var proxyPort: Int? = null

    @Required
    var codeOnPrepare: String = ""

    @Required
    var codeOnSuccess: String = ""

    @Required
    var codeOnFailure: String = ""

    fun allowsBody(): Boolean =
        METHOD_POST == method
            || METHOD_PUT == method
            || METHOD_DELETE == method
            || METHOD_PATCH == method
            || METHOD_OPTIONS == method

    fun isFeedbackErrorsOnly() =
        feedback == FEEDBACK_TOAST_ERRORS || feedback == FEEDBACK_TOAST_SIMPLE_ERRORS

    fun usesBasicAuthentication() = authentication == AUTHENTICATION_BASIC

    fun usesDigestAuthentication() = authentication == AUTHENTICATION_DIGEST

    fun usesBearerAuthentication() = authentication == AUTHENTICATION_BEARER

    fun usesRequestParameters() = allowsBody() && (requestBodyType == REQUEST_BODY_TYPE_FORM_DATA || requestBodyType == REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE)

    fun usesCustomBody() = allowsBody() && requestBodyType == REQUEST_BODY_TYPE_CUSTOM_TEXT

    fun usesFileBody() = allowsBody() && requestBodyType == REQUEST_BODY_TYPE_FILE

    fun isSameAs(other: Shortcut): Boolean {
        if (other.name != name ||
            other.bodyContent != bodyContent ||
            other.description != description ||
            other.feedback != feedback ||
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
            other.proxyHost != proxyHost ||
            other.proxyPort != proxyPort
        ) {
            return false
        }
        if (other.parameters.indices.any { !parameters[it]!!.isSameAs(other.parameters[it]!!) }) {
            return false
        }
        if (other.headers.indices.any { !headers[it]!!.isSameAs(other.headers[it]!!) }) {
            return false
        }
        return true
    }

    val isFeedbackUsingUI
        get() = isFeedbackInWindow || isFeedbackInDialog

    val isFeedbackInWindow
        get() = type.usesResponse && (feedback == FEEDBACK_ACTIVITY || feedback == FEEDBACK_DEBUG)

    val isFeedbackInDialog
        get() = type.usesResponse && feedback == FEEDBACK_DIALOG

    var isWaitForNetwork
        get() = retryPolicy == RETRY_POLICY_WAIT_FOR_INTERNET
        set(value) {
            retryPolicy = if (value) RETRY_POLICY_WAIT_FOR_INTERNET else RETRY_POLICY_NONE
        }

    val usesResponseBody: Boolean
        get() = type.usesResponse
            && (
            (feedback != FEEDBACK_TOAST_SIMPLE && feedback != FEEDBACK_NONE)
                || codeOnSuccess.isNotEmpty() || codeOnFailure.isNotEmpty()
            )

    companion object {

        const val TEMPORARY_ID: String = "0"
        const val NAME_MAX_LENGTH = 50

        const val FIELD_NAME = "name"

        const val METHOD_GET = "GET"
        const val METHOD_POST = "POST"
        const val METHOD_PUT = "PUT"
        const val METHOD_DELETE = "DELETE"
        const val METHOD_PATCH = "PATCH"
        const val METHOD_HEAD = "HEAD"
        const val METHOD_OPTIONS = "OPTIONS"
        const val METHOD_TRACE = "TRACE"

        const val FEEDBACK_NONE = "none"
        const val FEEDBACK_TOAST_SIMPLE = "simple_response"
        const val FEEDBACK_TOAST_SIMPLE_ERRORS = "simple_response_errors"
        const val FEEDBACK_TOAST = "full_response"
        const val FEEDBACK_TOAST_ERRORS = "errors_only"
        const val FEEDBACK_DIALOG = "dialog"
        const val FEEDBACK_ACTIVITY = "activity"
        const val FEEDBACK_DEBUG = "debug"

        private const val RETRY_POLICY_NONE = "none"
        private const val RETRY_POLICY_WAIT_FOR_INTERNET = "wait_for_internet"

        const val REQUEST_BODY_TYPE_FORM_DATA = "form_data"
        const val REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE = "x_www_form_urlencode"
        const val REQUEST_BODY_TYPE_CUSTOM_TEXT = "custom_text"
        const val REQUEST_BODY_TYPE_FILE = "file"

        const val AUTHENTICATION_NONE = "none"
        const val AUTHENTICATION_BASIC = "basic"
        const val AUTHENTICATION_DIGEST = "digest"
        const val AUTHENTICATION_BEARER = "bearer"

        const val DEFAULT_CONTENT_TYPE = "text/plain"
    }

}
