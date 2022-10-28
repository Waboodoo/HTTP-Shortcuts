package ch.rmy.android.http_shortcuts.extensions

import android.app.Dialog
import android.content.Context
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.framework.extensions.showIfPossible
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
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

fun createDialogState(id: String? = null, onDismiss: (() -> Unit)? = null, transform: DialogBuilder.() -> Dialog): DialogState =
    object : DialogState {
        override val id: String?
            get() = id

        override fun createDialog(context: Context, viewModel: WithDialog?) =
            DialogBuilder(context)
                .dismissListener {
                    onDismiss?.invoke()
                    viewModel?.onDialogDismissed(this)
                }
                .transform()
    }
