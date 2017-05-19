package ch.rmy.android.http_shortcuts.utils;

import ch.rmy.android.http_shortcuts.BuildConfig;

public class UserAgentUtil {

    public static String getUserAgent() {
        String base = "HttpShortcuts/" + BuildConfig.VERSION_NAME;
        String userAgent = System.getProperty("http.agent");
        int start = userAgent.indexOf("(");
        int end = userAgent.indexOf(")");
        if (start == -1 || end == -1 || start > end) {
            return base;
        }
        return base + " " + userAgent.substring(start, end + 1);
    }

}
