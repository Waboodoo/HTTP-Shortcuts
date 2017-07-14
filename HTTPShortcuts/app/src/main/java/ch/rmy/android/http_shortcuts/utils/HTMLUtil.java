package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;

public class HTMLUtil {

    public static Spanned getHTML(Context context, @StringRes int stringRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(context.getString(stringRes), 0);
        } else {
            return Html.fromHtml(context.getString(stringRes));
        }
    }

}
