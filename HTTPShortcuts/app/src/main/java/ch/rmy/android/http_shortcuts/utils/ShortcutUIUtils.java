package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class ShortcutUIUtils {

    private static final int[] FEEDBACK_RESOURCES = {R.string.feedback_none, R.string.feedback_simple_toast, R.string.feedback_simple_toast_error, R.string.feedback_response_toast, R.string.feedback_response_toast_error, R.string.feedback_dialog, R.string.feedback_activity};
    private static final int[] TIMEOUT_RESOURCES = {R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long};
    private static final int[] RETRY_POLICY_RESOURCES = {R.string.retry_policy_none, R.string.retry_policy_delayed};
    private static final int[] AUTHENTICATION_RESOURCES = {R.string.authentication_none, R.string.authentication_basic, R.string.authentication_digest};
    public static final int DEFAULT_ICON = R.drawable.ic_launcher;

    public static String[] getFeedbackOptions(Context context) {
        return getOptions(context, Shortcut.FEEDBACK_OPTIONS, FEEDBACK_RESOURCES);
    }

    public static String[] getRetryPolicyOptions(Context context) {
        return getOptions(context, Shortcut.RETRY_POLICY_OPTIONS, RETRY_POLICY_RESOURCES);
    }

    public static String[] getAuthenticationOptions(Context context) {
        return getOptions(context, Shortcut.AUTHENTICATION_OPTIONS, AUTHENTICATION_RESOURCES);
    }

    public static String[] getTimeoutOptions(Context context) {
        String[] timeoutStrings = new String[Shortcut.TIMEOUT_OPTIONS.length];
        for (int i = 0; i < Shortcut.TIMEOUT_OPTIONS.length; i++) {
            String timeName = context.getString(TIMEOUT_RESOURCES[i]);
            int seconds = Shortcut.TIMEOUT_OPTIONS[i] / 1000;
            String secondsString = context.getResources().getQuantityString(R.plurals.timeout_seconds, seconds, seconds);
            timeoutStrings[i] = context.getString(R.string.timeout_format, timeName, secondsString);
        }
        return timeoutStrings;
    }

    private static String[] getOptions(Context context, String[] keys, int[] valueResources) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = context.getString(valueResources[i]);
        }
        return values;
    }

}
