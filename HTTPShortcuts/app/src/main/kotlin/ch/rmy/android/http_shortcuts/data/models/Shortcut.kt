package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Shortcut : RealmObject(), HasId {

    @PrimaryKey
    override var id: String = ""
    @Required
    var name: String = ""
    @Required
    var method = METHOD_GET
    @Required
    var url: String = ""
    @Required
    var username: String = ""
    @Required
    var password: String = ""
    var iconName: String? = null
    @Required
    var feedback: String = ""
    @Required
    var description: String = ""
    @Required
    var bodyContent: String = ""
    var timeout: Int = 0
    @Required
    var retryPolicy: String = ""
    var headers: RealmList<Header> = RealmList()
    var parameters: RealmList<Parameter> = RealmList()
    var acceptAllCertificates: Boolean = false
    var authentication: String? = AUTHENTICATION_NONE
    var launcherShortcut: Boolean = false
    var delay: Int = 0
    @Required
    var requestBodyType: String = REQUEST_BODY_TYPE_CUSTOM_TEXT
    @Required
    var contentType: String = ""
    var executionType: String? = ""
    var requireConfirmation: Boolean = false
    var followRedirects: Boolean = true

    var serializedBeforeActions: String? = "[]"
    var serializedSuccessActions: String? = "[]"
    var serializedFailureActions: String? = "[]"

    fun allowsBody(): Boolean =
        METHOD_POST == method
            || METHOD_PUT == method
            || METHOD_DELETE == method
            || METHOD_PATCH == method
            || METHOD_OPTIONS == method

    fun isFeedbackErrorsOnly() =
        feedback == FEEDBACK_TOAST_ERRORS || feedback == FEEDBACK_TOAST_SIMPLE_ERRORS

    val isRetryAllowed
        get() = feedback != FEEDBACK_ACTIVITY && feedback != FEEDBACK_DIALOG && !isBrowserShortcut

    fun usesBasicAuthentication() = authentication == AUTHENTICATION_BASIC

    fun usesDigestAuthentication() = authentication == AUTHENTICATION_DIGEST

    fun usesAuthentication() = usesBasicAuthentication() || usesDigestAuthentication()

    fun usesRequestParameters() = allowsBody() && (requestBodyType == REQUEST_BODY_TYPE_FORM_DATA || requestBodyType == REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE)

    fun usesCustomBody() = allowsBody() && requestBodyType == REQUEST_BODY_TYPE_CUSTOM_TEXT

    fun isSameAs(other: Shortcut): Boolean {
        if (other.name != name ||
            other.bodyContent != bodyContent ||
            other.description != description ||
            other.feedback != feedback ||
            other.iconName != iconName ||
            other.method != method ||
            other.password != password ||
            other.retryPolicy != retryPolicy ||
            other.timeout != timeout ||
            other.url != url ||
            other.username != username ||
            other.authentication != authentication ||
            other.launcherShortcut != launcherShortcut ||
            other.acceptAllCertificates != acceptAllCertificates ||
            other.delay != delay ||
            other.parameters.size != parameters.size ||
            other.headers.size != headers.size ||
            other.requestBodyType != requestBodyType ||
            other.contentType != contentType ||
            other.serializedBeforeActions != serializedBeforeActions ||
            other.serializedSuccessActions != serializedSuccessActions ||
            other.serializedFailureActions != serializedFailureActions ||
            other.followRedirects != followRedirects
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

    var beforeActions: List<ActionDTO>
        get() = GsonUtil.parseActionList(serializedBeforeActions)
        set(value) {
            serializedBeforeActions = GsonUtil.toJson(value)
        }

    var successActions: List<ActionDTO>
        get() = GsonUtil.parseActionList(serializedSuccessActions)
        set(value) {
            serializedSuccessActions = GsonUtil.toJson(value)
        }

    var failureActions: List<ActionDTO>
        get() = GsonUtil.parseActionList(serializedFailureActions)
        set(value) {
            serializedFailureActions = GsonUtil.toJson(value)
        }

    val isFeedbackUsesUI
        get() = isFeedbackInWindow || isFeedbackInDialog

    val isFeedbackInWindow
        get() = feedback == FEEDBACK_ACTIVITY && !isBrowserShortcut

    val isFeedbackInDialog
        get() = feedback == FEEDBACK_DIALOG && !isBrowserShortcut

    val isBrowserShortcut
        get() = executionType == EXECUTION_TYPE_BROWSER

    val isWaitForNetwork
        get() = retryPolicy == RETRY_POLICY_WAIT_FOR_INTERNET

    companion object {

        const val TEMPORARY_ID: String = "0"

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

        const val RETRY_POLICY_NONE = "none"
        const val RETRY_POLICY_WAIT_FOR_INTERNET = "wait_for_internet"

        const val REQUEST_BODY_TYPE_FORM_DATA = "form_data"
        const val REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE = "x_www_form_urlencode"
        const val REQUEST_BODY_TYPE_CUSTOM_TEXT = "custom_text"

        private const val EXECUTION_TYPE_APP = "app"
        private const val EXECUTION_TYPE_BROWSER = "browser"

        val FEEDBACK_OPTIONS = arrayOf(FEEDBACK_NONE, FEEDBACK_TOAST_SIMPLE, FEEDBACK_TOAST_SIMPLE_ERRORS, FEEDBACK_TOAST, FEEDBACK_TOAST_ERRORS, FEEDBACK_DIALOG, FEEDBACK_ACTIVITY)
        val TIMEOUT_OPTIONS = intArrayOf(3000, 10000, 30000, 60000, 180000, 300000, 600000)
        val DELAY_OPTIONS = intArrayOf(0, 5000, 10000, 30000, 60000, 120000, 300000, 600000)

        val RETRY_POLICY_OPTIONS = arrayOf(RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET)

        const val AUTHENTICATION_NONE = "none"
        const val AUTHENTICATION_BASIC = "basic"
        const val AUTHENTICATION_DIGEST = "digest"

        const val DEFAULT_CONTENT_TYPE = "text/plain"

        fun createNew(id: String = "", iconName: String? = null, browserShortcut: Boolean = false) = Shortcut().apply {
            this.id = id
            this.iconName = iconName
            name = ""
            description = ""
            username = ""
            password = ""
            bodyContent = ""
            method = METHOD_GET
            url = "http://"
            timeout = TIMEOUT_OPTIONS[1]
            feedback = FEEDBACK_TOAST_SIMPLE
            retryPolicy = RETRY_POLICY_NONE
            authentication = AUTHENTICATION_NONE
            delay = 0
            parameters = RealmList()
            headers = RealmList()
            requestBodyType = REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE
            contentType = DEFAULT_CONTENT_TYPE
            serializedBeforeActions = "[]"
            serializedSuccessActions = "[]"
            serializedFailureActions = "[]"
            followRedirects = true
            executionType = if (browserShortcut) EXECUTION_TYPE_BROWSER else EXECUTION_TYPE_APP
        }
    }

}
