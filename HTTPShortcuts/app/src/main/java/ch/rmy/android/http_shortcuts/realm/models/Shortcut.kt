package ch.rmy.android.http_shortcuts.realm.models

import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Shortcut : RealmObject(), HasId {

    @PrimaryKey
    override var id: Long = 0
    @Required
    var name: String? = null
    @Required
    var method = METHOD_GET
    @Required
    var url: String? = null
    @Required
    var username: String? = null
    @Required
    var password: String? = null
    var iconName: String? = null
    @Required
    var feedback: String? = null
    @Required
    var description: String? = null
    @Required
    var bodyContent: String? = null
    var timeout: Int = 0
    @Required
    var retryPolicy: String? = null
    var headers: RealmList<Header>? = null
    var parameters: RealmList<Parameter>? = null
    var acceptAllCertificates: Boolean = false
    var authentication: String? = AUTHENTICATION_NONE
    var launcherShortcut: Boolean = false
    var delay: Int = 0

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

        duplicate.parameters = RealmList<Parameter>()
        for (parameter in parameters!!) {
            duplicate.parameters!!.add(Parameter.createNew(parameter.key!!, parameter.value!!))
        }

        duplicate.headers = RealmList<Header>()
        for (header in headers!!) {
            duplicate.headers!!.add(Header.createNew(header.key!!, header.value!!))
        }

        return duplicate
    }

    fun getIconURI(context: Context): Uri {
        val packageName = context.packageName
        return if (iconName == null) {
            Uri.parse("android.resource://" + packageName + "/" + ShortcutUIUtils.DEFAULT_ICON)
        } else if (iconName!!.startsWith("android.resource://")) {
            Uri.parse(iconName)
        } else if (iconName!!.endsWith(".png")) {
            Uri.fromFile(context.getFileStreamPath(iconName))
        } else {
            val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
            Uri.parse("android.resource://$packageName/$identifier")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun getIcon(context: Context): Icon? {
        try {
            val packageName = context.packageName
            return if (iconName == null) {
                Icon.createWithResource(packageName, ShortcutUIUtils.DEFAULT_ICON)
            } else if (iconName!!.startsWith("android.resource://")) {
                val pathSegments = Uri.parse(iconName).pathSegments
                Icon.createWithResource(pathSegments[0], Integer.parseInt(pathSegments[1]))
            } else if (iconName!!.endsWith(".png")) {
                null // TODO: Generate Icon from Bitmap
            } else {
                val identifier = context.resources.getIdentifier(iconName, "drawable", context.packageName)
                return Icon.createWithResource(packageName, identifier)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getSafeName(context: Context): String {
        if (Validation.isEmpty(name!!)) {
            return context.getString(R.string.shortcut_safe_name)
        }
        return name!!
    }

    fun allowsBody(): Boolean {
        return METHOD_POST == method
                || METHOD_PUT == method
                || METHOD_DELETE == method
                || METHOD_PATCH == method
                || METHOD_OPTIONS == method
    }

    fun feedbackUsesUI(): Boolean {
        return FEEDBACK_DIALOG == feedback || FEEDBACK_ACTIVITY == feedback
    }

    fun isFeedbackErrorsOnly(): Boolean {
        return FEEDBACK_TOAST_ERRORS == feedback || FEEDBACK_TOAST_SIMPLE_ERRORS == feedback
    }

    fun isRetryAllowed(): Boolean {
        return FEEDBACK_ACTIVITY != feedback && FEEDBACK_DIALOG != feedback
    }

    fun usesAuthentication(): Boolean {
        return usesBasicAuthentication() || usesDigestAuthentication()
    }

    fun usesBasicAuthentication(): Boolean {
        return AUTHENTICATION_BASIC == authentication
    }

    fun usesDigestAuthentication(): Boolean {
        return AUTHENTICATION_DIGEST == authentication
    }

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
                other.parameters!!.size != parameters!!.size ||
                other.headers!!.size != headers!!.size
                ) {
            return false;
        }
        if (other.parameters!!.indices.any { !parameters!![it].isSameAs(other.parameters!![it]) }) {
            return false;
        }
        if (other.headers!!.indices.any { !headers!![it].isSameAs(other.headers!![it]) }) {
            return false;
        }
        return true;
    }

    companion object {

        const val TEMPORARY_ID: Long = -1

        const val FIELD_NAME = "name"
        const val FIELD_LAUNCHER_SHORTCUT = "launcherShortcut"

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

        val METHODS = arrayOf(METHOD_GET, METHOD_POST, METHOD_PUT, METHOD_DELETE, METHOD_PATCH, METHOD_HEAD, METHOD_OPTIONS, METHOD_TRACE)
        val FEEDBACK_OPTIONS = arrayOf(FEEDBACK_NONE, FEEDBACK_TOAST_SIMPLE, FEEDBACK_TOAST_SIMPLE_ERRORS, FEEDBACK_TOAST, FEEDBACK_TOAST_ERRORS, FEEDBACK_DIALOG, FEEDBACK_ACTIVITY)
        val TIMEOUT_OPTIONS = intArrayOf(3000, 10000, 30000, 60000)
        val DELAY_OPTIONS = intArrayOf(0, 5000, 10000, 30000, 60000, 120000, 300000, 600000)

        val RETRY_POLICY_OPTIONS = arrayOf(RETRY_POLICY_NONE, RETRY_POLICY_WAIT_FOR_INTERNET)

        const val AUTHENTICATION_NONE = "none"
        const val AUTHENTICATION_BASIC = "basic"
        const val AUTHENTICATION_DIGEST = "digest"

        val AUTHENTICATION_OPTIONS = arrayOf<String>(AUTHENTICATION_NONE, AUTHENTICATION_BASIC, AUTHENTICATION_DIGEST)

        fun createNew(): Shortcut {
            val shortcut = Shortcut()
            shortcut.id = 0
            shortcut.name = ""
            shortcut.description = ""
            shortcut.username = ""
            shortcut.password = ""
            shortcut.bodyContent = ""
            shortcut.method = METHOD_GET
            shortcut.url = "http://"
            shortcut.timeout = TIMEOUT_OPTIONS[1]
            shortcut.feedback = FEEDBACK_TOAST_SIMPLE
            shortcut.retryPolicy = RETRY_POLICY_NONE
            shortcut.authentication = AUTHENTICATION_NONE
            shortcut.delay = 0
            shortcut.parameters = RealmList<Parameter>()
            shortcut.headers = RealmList<Header>()
            return shortcut
        }
    }

}
