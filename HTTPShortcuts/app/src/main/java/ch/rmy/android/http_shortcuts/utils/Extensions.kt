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
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Header
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.curlcommand.CurlCommand
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner
import org.apache.http.HttpHeaders
import org.jdeferred2.Deferred
import org.jdeferred2.DoneFilter
import org.jdeferred2.FailFilter
import org.jdeferred2.ProgressFilter
import org.jdeferred2.Promise

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
    MaterialDialog.Builder(context!!)
            .content(stringRes)
            .positiveText(R.string.dialog_ok)
            .showIfPossible()
}

fun EditText.focus() {
    requestFocus()
    try {
        setSelection(text.length)
    } catch (e: Exception) {
        logException(e)
    }
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

fun Any.logException(e: Throwable) {
    if (CrashReporting.enabled) {
        CrashReporting.logException(e)
    } else {
        Log.e(this.javaClass.simpleName, "An error occurred", e)
    }
}

fun View.showSoftKeyboard() {
    requestFocus()
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.showToast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes message: Int, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun <T, U, F, P> Promise<T, F, P>.filter(filter: (T) -> U) = this.then(DoneFilter<T, U> { result -> filter(result) }, null as FailFilter<F, F>?, null as ProgressFilter<P, P>?)!!

fun <T, U, V> Deferred<T, U, V>.rejectSafely(reject: U): Deferred<T, U, V> {
    return if (isPending) {
        reject(reject)
    } else {
        this
    }
}

fun CurlCommand.applyToShortcut(shortcut: Shortcut) {
    shortcut.url = url
    shortcut.method = method
    shortcut.bodyContent = data
    shortcut.requestBodyType = Shortcut.REQUEST_BODY_TYPE_CUSTOM_TEXT
    shortcut.username = username
    shortcut.password = password
    if (!username.isNullOrEmpty() || !password.isNullOrEmpty()) {
        shortcut.authentication = Shortcut.AUTHENTICATION_BASIC
    }
    if (timeout != 0) {
        shortcut.timeout = timeout
    }
    for ((key, value) in headers) {
        if (key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
            shortcut.contentType = value
        } else {
            shortcut.headers.add(Header.createNew(key, value))
        }
    }
}

fun EditText.onTextChanged(listener: (text: CharSequence) -> Unit): Destroyable {
    val watcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            listener.invoke(s)
        }
    }
    addTextChangedListener(watcher)
    listener.invoke(text)
    return object : Destroyable {
        override fun destroy() {
            removeTextChangedListener(watcher)
        }
    }
}