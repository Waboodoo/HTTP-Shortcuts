package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.util.Predicate
import androidx.fragment.app.Fragment
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.Destroyable
import ch.rmy.android.http_shortcuts.utils.Destroyer
import io.reactivex.disposables.Disposable

fun Context.showMessageDialog(@StringRes stringRes: Int, onDismiss: () -> Unit = {}) {
    showMessageDialog(getString(stringRes), onDismiss)
}

fun Context.showMessageDialog(string: CharSequence, onDismiss: () -> Unit = {}) {
    DialogBuilder(this)
        .message(string)
        .positive(R.string.dialog_ok)
        .dismissListener(onDismiss)
        .showIfPossible()
}

fun Fragment.showMessageDialog(@StringRes stringRes: Int, onDismiss: () -> Unit = {}) {
    requireContext().showMessageDialog(stringRes, onDismiss)
}

fun Fragment.showMessageDialog(string: CharSequence, onDismiss: () -> Unit = {}) {
    requireContext().showMessageDialog(string, onDismiss)
}

@ColorInt
fun color(context: Context, @ColorRes colorRes: Int): Int = ContextCompat.getColor(context, colorRes)

fun drawable(context: Context, @DrawableRes drawableRes: Int): Drawable? =
    AppCompatResources.getDrawable(context, drawableRes)

fun Activity.dimen(@DimenRes dimenRes: Int) = dimen(this, dimenRes)
fun dimen(context: Context, @DimenRes dimenRes: Int) = context.resources.getDimensionPixelSize(dimenRes)

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

inline fun <T> T.mapIf(predicate: Boolean, block: T.() -> T): T =
    if (predicate) block(this) else this

inline fun <T, U> T.mapFor(iterable: Iterable<U>, block: T.(U) -> T): T {
    val iterator = iterable.iterator()
    var item = this
    while (iterator.hasNext()) {
        item = block.invoke(item, iterator.next())
    }
    return item
}

fun Disposable.toDestroyable() = object : Destroyable {
    override fun destroy() {
        dispose()
    }
}

fun Disposable.attachTo(destroyer: Destroyer) {
    destroyer.own { dispose() }
}

fun <T> Map<String, T>.getCaseInsensitive(key: String): T? =
    entries.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value

inline fun <T> Boolean.ifTrue(block: () -> T): T? =
    if (this) {
        block.invoke()
    } else {
        null
    }

fun <T> MutableCollection<T>.safeRemoveIf(predicate: Predicate<T>) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        removeIf(predicate::test)
    } else {
        val iterator = iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (predicate.test(item)) {
                iterator.remove()
            }
        }
    }
}
