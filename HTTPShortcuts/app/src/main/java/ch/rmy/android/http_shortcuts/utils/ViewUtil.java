package ch.rmy.android.http_shortcuts.utils;

import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.satsuware.usefulviews.LabelledSpinner;

import ch.rmy.android.http_shortcuts.R;

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

    public static void fixLabelledSpinner(LabelledSpinner spinner) {
        int paddingTop = spinner.getContext().getResources().getDimensionPixelSize(R.dimen.spinner_padding_top);
        spinner.getLabel().setPadding(0, paddingTop, 0, 0);
        spinner.getErrorLabel().setVisibility(View.GONE);
    }

}
