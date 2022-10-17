package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.coroutines.suspendCancellableCoroutine

fun DialogBuilder.showIfPossible(): MaterialDialog? =
    build().showIfPossible()

fun DialogBuilder.showOrElse(block: () -> Unit) {
    showIfPossible() ?: block()
}

suspend fun DialogBuilder.showAndAwaitDismissal() {
    suspendCancellableCoroutine<Unit> { continuation ->
        dismissListener {
            if (continuation.isActive) {
                continuation.resume()
            }
        }
            .showIfPossible()
            ?: let {
                if (continuation.isActive) {
                    continuation.resume()
                }
            }
    }
}
