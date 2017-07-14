package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.satsuware.usefulviews.LabelledSpinner;

import ch.rmy.android.http_shortcuts.R;

public class UIUtil {

    public static void focus(EditText view) {
        view.requestFocus();
        view.setSelection(view.getText().length());
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(drawableRes, context.getTheme());
        } else {
            return context.getResources().getDrawable(drawableRes);
        }
    }

    @ColorInt
    @SuppressWarnings("deprecation")
    public static int getColor(Context context, @ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(colorRes, context.getTheme());
        } else {
            return context.getResources().getColor(colorRes);
        }
    }

}
