package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import ch.rmy.android.http_shortcuts.R
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner

var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

fun Fragment.showMessageDialog(@StringRes stringRes: Int) {
    if ((context as? Activity)?.isFinishing == true) {
        return
    }
    MaterialDialog.Builder(context!!)
            .content(stringRes)
            .positiveText(R.string.dialog_ok)
            .show()
}

fun EditText.focus() {
    requestFocus()
    setSelection(text.length)
}

@Suppress("DEPRECATION")
fun ImageView.clearBackground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background = null
    } else {
        setBackgroundDrawable(null)
    }
}

fun LabelledSpinner.fix() {
    val paddingTop = spinner.context.resources.getDimensionPixelSize(R.dimen.spinner_padding_top)
    label.setPadding(0, paddingTop, 0, 0)
    errorLabel.visibility = View.GONE
}

@ColorInt
fun color(context: Context, @ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

fun drawable(context: Context, @DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(context, drawableRes)

fun Activity.dimen(@DimenRes dimenRes: Int) = dimen(this, dimenRes)
fun dimen(context: Context, @DimenRes dimenRes: Int) = context.resources.getDimensionPixelSize(dimenRes)

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

inline fun <T> T.mapIf(predicate: Boolean, block: (T) -> T): T = if (predicate) block(this) else this

inline fun <T, U> T.mapFor(iterable: Iterable<U>, block: (T, U) -> T): T {
    val iterator = iterable.iterator()
    var item = this
    while (iterator.hasNext()) {
        item = block.invoke(item, iterator.next())
    }
    return item
}