package ch.rmy.android.http_shortcuts.utils;

import android.text.TextUtils;
import android.webkit.URLUtil;

public class Validation {

    public static boolean isEmpty(String string) {
        return string.matches("^\\s*$");
    }

    public static boolean isValidUrl(String url) {
        if (TextUtils.isEmpty(url) || url.equalsIgnoreCase("http://") || url.equalsIgnoreCase("https://")) {
            return false;
        }
        return URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url);
    }

}
