package ch.rmy.android.http_shortcuts.utils

import android.content.Context

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable

object ShortcutUIUtils {

    private val EXECUTION_TYPE_RESOURCES = intArrayOf(R.string.execution_type_option_in_app, R.string.execution_type_option_in_browser)
    private val FEEDBACK_RESOURCES = intArrayOf(R.string.feedback_none, R.string.feedback_simple_toast, R.string.feedback_simple_toast_error, R.string.feedback_response_toast, R.string.feedback_response_toast_error, R.string.feedback_dialog, R.string.feedback_activity)
    private val TIMEOUT_RESOURCES = intArrayOf(R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long, R.string.timeout_very_long, R.string.timeout_very_long, R.string.timeout_very_long)
    private val RETRY_POLICY_RESOURCES = intArrayOf(R.string.retry_policy_none, R.string.retry_policy_delayed)
    private val AUTHENTICATION_RESOURCES = intArrayOf(R.string.authentication_none, R.string.authentication_basic, R.string.authentication_digest)
    private val REQUEST_BODY_RESOURCES = intArrayOf(R.string.request_body_option_form_data, R.string.request_body_option_x_www_form_urlencoded, R.string.request_body_option_custom_text)
    val DEFAULT_ICON = R.drawable.ic_launcher

    fun getExecutionTypeOptions(context: Context) =
            getOptions(context, Shortcut.EXECUTION_TYPES, EXECUTION_TYPE_RESOURCES)

    fun getFeedbackOptions(context: Context) =
            getOptions(context, Shortcut.FEEDBACK_OPTIONS, FEEDBACK_RESOURCES)

    fun getRetryPolicyOptions(context: Context) =
            getOptions(context, Shortcut.RETRY_POLICY_OPTIONS, RETRY_POLICY_RESOURCES)

    fun getAuthenticationOptions(context: Context) =
            getOptions(context, Shortcut.AUTHENTICATION_OPTIONS, AUTHENTICATION_RESOURCES)

    fun getVariableTypeOptions(context: Context) =
            getOptions(context, Variable.TYPE_OPTIONS, Variable.TYPE_RESOURCES)

    fun getRequestBodyTypeOptions(context: Context) =
            getOptions(context, Shortcut.REQUEST_BODY_TYPE_OPTIONS, REQUEST_BODY_RESOURCES)

    fun getTimeoutOptions(context: Context): Array<String> =
            Array(Shortcut.TIMEOUT_OPTIONS.size) { i ->
                val timeName = context.getString(TIMEOUT_RESOURCES[i])
                val timeString = getTimeString(context, Shortcut.TIMEOUT_OPTIONS[i] / 1000)
                context.getString(R.string.timeout_format, timeName, timeString)
            }

    fun getDelayOptions(context: Context): Array<String> =
            Array(Shortcut.DELAY_OPTIONS.size) { i ->
                getTimeString(context, Shortcut.DELAY_OPTIONS[i] / 1000)
            }

    private fun getTimeString(context: Context, seconds: Int) =
            when {
                seconds == 0 -> context.resources.getString(R.string.label_no_delay)
                seconds < 60 -> context.resources.getQuantityString(R.plurals.seconds, seconds, seconds)
                else -> {
                    val minutes = seconds / 60
                    context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)
                }
            }

    private fun getOptions(context: Context, keys: Array<String>, valueResources: IntArray): Array<String> =
            Array(keys.size) { i -> context.getString(valueResources[i]) }

}
