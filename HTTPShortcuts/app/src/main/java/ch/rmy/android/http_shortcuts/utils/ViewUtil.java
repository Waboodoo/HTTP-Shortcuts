package ch.rmy.android.http_shortcuts.utils;

import android.os.Build;
import android.widget.EditText;
import android.widget.ImageView;

public class ViewUtil {

    public static void focus(EditText view) {
        view.requestFocus();
        view.setSelection(view.getText().length());
    }

    public static void clearBackground(ImageView view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(null);
        } else {
            view.setBackgroundDrawable(null);
        }
    }

}
