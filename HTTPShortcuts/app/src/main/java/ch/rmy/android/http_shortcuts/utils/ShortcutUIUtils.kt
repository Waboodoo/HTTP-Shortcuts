package ch.rmy.android.http_shortcuts.utils

import android.content.Context

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Shortcut

object ShortcutUIUtils {

    private val FEEDBACK_RESOURCES = intArrayOf(R.string.feedback_none, R.string.feedback_simple_toast, R.string.feedback_simple_toast_error, R.string.feedback_response_toast, R.string.feedback_response_toast_error, R.string.feedback_dialog, R.string.feedback_activity)
    private val TIMEOUT_RESOURCES = intArrayOf(R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long)
    private val RETRY_POLICY_RESOURCES = intArrayOf(R.string.retry_policy_none, R.string.retry_policy_delayed)
    private val AUTHENTICATION_RESOURCES = intArrayOf(R.string.authentication_none, R.string.authentication_basic, R.string.authentication_digest)
    val DEFAULT_ICON = R.drawable.ic_launcher

    fun getFeedbackOptions(context: Context) = getOptions(context, Shortcut.FEEDBACK_OPTIONS, FEEDBACK_RESOURCES)

    fun getRetryPolicyOptions(context: Context) = getOptions(context, Shortcut.RETRY_POLICY_OPTIONS, RETRY_POLICY_RESOURCES)

    fun getAuthenticationOptions(context: Context) = getOptions(context, Shortcut.AUTHENTICATION_OPTIONS, AUTHENTICATION_RESOURCES)

    fun getTimeoutOptions(context: Context): Array<String> {
        return Array<String>(Shortcut.TIMEOUT_OPTIONS.size, { i ->
            val timeName = context.getString(TIMEOUT_RESOURCES[i])
            val seconds = Shortcut.TIMEOUT_OPTIONS[i] / 1000
            val secondsString = context.resources.getQuantityString(R.plurals.timeout_seconds, seconds, seconds)
            context.getString(R.string.timeout_format, timeName, secondsString)
        })
    }

    private fun getOptions(context: Context, keys: Array<String>, valueResources: IntArray): Array<String> {
        return Array<String>(keys.size, { i -> context.getString(valueResources[i]) })
    }

}
