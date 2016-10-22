package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

public class ShortcutUIUtils {

    private static final int[] FEEDBACK_RESOURCES = {R.string.feedback_none, R.string.feedback_simple_toast, R.string.feedback_response_toast, R.string.feedback_dialog};
    private static final int[] TIMEOUT_RESOURCES = {R.string.timeout_short, R.string.timeout_medium, R.string.timeout_long, R.string.timeout_very_long};
    private static final int[] RETRY_POLICY_RESOURCES = {R.string.retry_policy_none, R.string.retry_policy_delayed};
    public static final int DEFAULT_ICON = R.drawable.ic_launcher;

    public static String[] getFeedbackOptions(Context context) {
        String[] feedbackStrings = new String[Shortcut.FEEDBACK_OPTIONS.length];
        for (int i = 0; i < Shortcut.FEEDBACK_OPTIONS.length; i++) {
            feedbackStrings[i] = context.getString(FEEDBACK_RESOURCES[i]);
        }
        return feedbackStrings;
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

    public static String[] getRetryPolicyOptions(Context context) {
        String[] retryPolicyStrings = new String[Shortcut.RETRY_POLICY_OPTIONS.length];
        for (int i = 0; i < Shortcut.RETRY_POLICY_OPTIONS.length; i++) {
            retryPolicyStrings[i] = context.getString(RETRY_POLICY_RESOURCES[i]);
        }
        return retryPolicyStrings;
    }

}
