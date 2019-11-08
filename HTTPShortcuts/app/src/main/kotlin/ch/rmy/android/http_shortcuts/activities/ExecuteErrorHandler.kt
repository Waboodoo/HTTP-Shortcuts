package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.utils.CanceledByUserException
import io.reactivex.Completable
import org.liquidplayer.javascript.JSException

class ExecuteErrorHandler(private val context: Context) {

    fun handleError(error: Throwable): Completable =
        if (isUserError(error)) {
            showUserError(error)
                .andThen(Completable.error(error))
        } else {
            if (error !is CanceledByUserException) {
                logException(error) // TODO: This needs to be removed
            }
            Completable.error(error)
        }

    private fun isUserError(error: Throwable) =
        error is JSException

    private fun showUserError(error: Throwable): Completable =
        Completable.create { emitter ->
            DialogBuilder(context)
                .title(R.string.dialog_title_error)
                .message(getErrorMessage(error))
                .dismissListener {
                    emitter.onComplete()
                }
                .positive(R.string.dialog_ok)
                .showIfPossible()
                ?: run {
                    emitter.onComplete()
                }
        }

    private fun getErrorMessage(error: Throwable): String =
        when (error) {
            is JSException -> {
                context.getString(R.string.error_js_pattern, getBasicErrorMessage(error))
            }
            else -> getBasicErrorMessage(error)
        }

    private fun getBasicErrorMessage(error: Throwable): String =
        error.message ?: context.getString(R.string.error_generic)

}