package ch.rmy.android.http_shortcuts.utils;

import ch.rmy.android.http_shortcuts.BuildConfig;

public class UserAgentUtil {

    public static String getUserAgent() {
        String userAgent = System.getProperty("http.agent");
        int start = userAgent.indexOf("(");
        int end = userAgent.indexOf(")");
        return "HttpShortcuts/" + BuildConfig.VERSION_NAME + " " + userAgent.substring(start, end + 1);
    }

}
