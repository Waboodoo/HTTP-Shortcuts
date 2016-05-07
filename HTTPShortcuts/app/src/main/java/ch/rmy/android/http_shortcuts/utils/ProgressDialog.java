package ch.rmy.android.http_shortcuts.utils;

import android.app.Dialog;
import android.content.Context;

public class ProgressDialog {

    public static Dialog show(Context context, int textResId) {
        return android.app.ProgressDialog.show(context, null, context.getString(textResId));
    }

}
