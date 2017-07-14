package ch.rmy.android.http_shortcuts.utils;

import android.text.TextUtils;

public class Validation {

    public static boolean isEmpty(String string) {
        return string.matches("^\\s*$");
    }

    public static boolean isAcceptableUrl(String url) {
        return !TextUtils.isEmpty(url) && !url.equalsIgnoreCase("http://") && !url.equalsIgnoreCase("https://");
    }

}
