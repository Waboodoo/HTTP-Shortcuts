package ch.rmy.android.http_shortcuts.utils;

import android.widget.EditText;

public class ViewUtil {

    public static void focus(EditText view) {
        view.requestFocus();
        view.setSelection(view.getText().length());
    }

}
