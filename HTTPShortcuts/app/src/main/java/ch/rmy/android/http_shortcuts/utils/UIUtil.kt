package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import ch.rmy.android.http_shortcuts.R
import com.satsuware.usefulviews.LabelledSpinner

object UIUtil {

    fun focus(view: EditText) {
        view.requestFocus()
        view.setSelection(view.text.length)
    }

    @Suppress("DEPRECATION")
    fun clearBackground(view: ImageView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = null
        } else {
            view.setBackgroundDrawable(null)
        }
    }

    fun fixLabelledSpinner(spinner: LabelledSpinner) {
        val paddingTop = spinner.context.resources.getDimensionPixelSize(R.dimen.spinner_padding_top)
        spinner.label.setPadding(0, paddingTop, 0, 0)
        spinner.errorLabel.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    fun getDrawable(context: Context, @DrawableRes drawableRes: Int): Drawable {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return context.resources.getDrawable(drawableRes, context.theme)
        } else {
            return context.resources.getDrawable(drawableRes)
        }
    }

    @ColorInt
    @Suppress("DEPRECATION")
    fun getColor(context: Context, @ColorRes colorRes: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.resources.getColor(colorRes, context.theme)
        } else {
            return context.resources.getColor(colorRes)
        }
    }

}
