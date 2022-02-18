package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder

@Deprecated("These should no longer be needed once all activity/fragments use MVI pattern")
fun Context.showMessageDialog(@StringRes stringRes: Int, onDismiss: () -> Unit = {}) {
    showMessageDialog(getString(stringRes), onDismiss)
}

@Deprecated("These should no longer be needed once all activity/fragments use MVI pattern")
fun Context.showMessageDialog(string: CharSequence, onDismiss: () -> Unit = {}) {
    DialogBuilder(this)
        .message(string)
        .positive(R.string.dialog_ok)
        .dismissListener(onDismiss)
        .showIfPossible()
}

@Deprecated("These should no longer be needed once all activity/fragments use MVI pattern")
fun Fragment.showMessageDialog(@StringRes stringRes: Int, onDismiss: () -> Unit = {}) {
    requireContext().showMessageDialog(stringRes, onDismiss)
}

@Deprecated("These should no longer be needed once all activity/fragments use MVI pattern")
fun Fragment.showMessageDialog(string: CharSequence, onDismiss: () -> Unit = {}) {
    requireContext().showMessageDialog(string, onDismiss)
}
