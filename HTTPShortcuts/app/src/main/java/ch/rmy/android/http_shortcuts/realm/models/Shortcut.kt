package ch.rmy.android.http_shortcuts.realm.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Shortcut : RealmObject(), HasId {

    @PrimaryKey
    override var id: Long = 0
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

    var beforeActions: String? = null
    var successActions: String? = null
    var failureActions: String? = null

    override val isNew: Boolean
        get() = id == 0L

    fun duplicate(newName: String): Shortcut {
        val duplicate = Shortcut()
        duplicate.id = 0
        duplicate.name = newName
        duplicate.bodyContent = bodyContent
        duplicate.description = description
        duplicate.feedback = feedback
        duplicate.iconName = iconName
        duplicate.method = method
        duplicate.password = password
        duplicate.retryPolicy = retryPolicy
        duplicate.timeout = timeout
        duplicate.url = url
        duplicate.username = username
        duplicate.authentication = authentication
        duplicate.launcherShortcut = launcherShortcut
        duplicate.acceptAllCertificates = acceptAllCertificates
        duplicate.delay = delay
        duplicate.requestBodyType = requestBodyType
        duplicate.contentType = contentType
        duplicate.beforeActions = beforeActions
        duplicate.successActions = successActions
        duplicate.failureActions = failureActions

        duplicate.parameters = RealmList()
        for (parameter in parameters) {
            duplicate.parameters.add(Parameter.createNew(parameter.key, parameter.value))
        }

        duplicate.headers = RealmList()
        for (header in headers) {
            duplicate.headers.add(Header.createNew(header.key, header.value))
        }

        return duplicate
    }

    fun getIconURI(context: Context): Uri {
        val packageName = context.packageName
        return when {
            iconName == null -> Uri.parse("android.resource://" + packageName + "/" + ShortcutUIUtils.DEFAULT_ICON)
            iconName!!.startsWith("android.resource://") -> Uri.parse(iconName)
            iconName!!.endsWith(".png") -> Uri.fromFile(context.getFileStreamPath(iconName))
            else -> {
                val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                Uri.parse("android.resource://$packageName/$identifier")
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getIcon(context: Context): Icon? = try {
        val packageName = context.packageName
        when {
            iconName == null -> Icon.createWithResource(packageName, ShortcutUIUtils.DEFAULT_ICON)
            iconName!!.startsWith("android.resource://") -> {
                val pathSegments = Uri.parse(iconName).pathSegments
                Icon.createWithResource(pathSegments[0], Integer.parseInt(pathSegments[1]))
            }
            iconName!!.endsWith(".png") -> {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val bitmap = BitmapFactory.decodeFile(context.getFileStreamPath(iconName).absolutePath, options)
                Icon.createWithBitmap(bitmap)
            }
            else -> {
                val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                Icon.createWithResource(packageName, identifier)
            }
        }
    } catch (e: Exception) {
        null
    }

    fun getSafeName(context: Context): String = if (name.isBlank()) {
        context.getString(R.string.shortcut_safe_name)
    } else {
        name
    }

    fun allowsBody(): Boolean {
        return METHOD_POST == method
                || METHOD_PUT == method
                || METHOD_DELETE == method
                || METHOD_PATCH == method
                || METHOD_OPTIONS == method
    }

    fun feedbackUsesUI() = feedback == FEEDBACK_DIALOG || feedback == FEEDBACK_ACTIVITY

    fun isFeedbackErrorsOnly() =
            feedback == FEEDBACK_TOAST_ERRORS || feedback == FEEDBACK_TOAST_SIMPLE_ERRORS

    fun isRetryAllowed() = feedback != FEEDBACK_ACTIVITY && feedback != FEEDBACK_DIALOG

    fun usesAuthentication() = usesBasicAuthentication() || usesDigestAuthentication()

    fun usesBasicAuthentication() = authentication == AUTHENTICATION_BASIC

    fun usesDigestAuthentication() = authentication == AUTHENTICATION_DIGEST

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
                other.beforeActions != beforeActions ||
                other.successActions != successActions ||
                other.failureActions != failureActions
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

    companion object {

        const val TEMPORARY_ID: Long = -1

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

        val METHODS = arrayOf(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_PATCH, METHOD_HEAD, METHOD_OPTIONS, METHOD_TRACE)
        val FEEDBACK_OPTIONS = arrayOf(FEEDBACK_NONE, FEEDBACK_TOAST_SIMPLE, FEEDBACK_TOAST_SIMPLE_ERRORS, FEEDBACK_TOAST, FEEDBACK_TOAST_ERRORS, FEEDBACK_DIALOG, FEEDBACK_ACTIVITY)
        val TIMEOUT_OPTIONS = intArrayOf(3000, 10000, 30000, 60000)
        val DELAY_OPTIONS = intArrayOf(0, 5000, 10000, 30000, 60000, 120000, 300000, 600000)

        val RETRY_POLICY_OPTIONS = arrayOf(RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET)

        const val AUTHENTICATION_NONE = "none"
        const val AUTHENTICATION_BASIC = "basic"
        const val AUTHENTICATION_DIGEST = "digest"

        val AUTHENTICATION_OPTIONS = arrayOf(AUTHENTICATION_NONE, AUTHENTICATION_BASIC, AUTHENTICATION_DIGEST)

        val REQUEST_BODY_TYPE_OPTIONS = arrayOf(REQUEST_BODY_TYPE_FORM_DATA, REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE, REQUEST_BODY_TYPE_CUSTOM_TEXT)

        val CONTENT_TYPE_SUGGESTIONS = arrayOf(
                "application/javascript",
                "application/json",
                "application/octet-stream",
                "application/xml",
                "text/css",
                "text/csv",
                "text/plain",
                "text/html",
                "text/xml"
        )

        const val DEFAULT_CONTENT_TYPE = "text/plain"

        fun createNew() = Shortcut().apply {
            id = 0
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
            beforeActions = "[]"
            successActions = "[]"
            failureActions = "[]"
        }
    }

}
