package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

@Deprecated("Use Compose instead")
class DialogBuilder(val context: Context) {

    private val dialog = MaterialDialog(context)

    fun title(title: String?) = also {
        if (title?.isNotEmpty() == true) {
            dialog.title(text = title)
        }
    }

    fun message(@StringRes text: Int, isHtml: Boolean = false) =
        message(context.getString(text), isHtml)

    fun message(text: CharSequence, isHtml: Boolean = false) = also {
        dialog.message(text = text) {
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
            if (isHtml) {
                html()
            }
        }
    }

    fun view(view: View) = also {
        dialog.customView(view = view)
    }

    fun dismissListener(onDismissListener: () -> Unit) = also {
        dialog.setOnDismissListener { onDismissListener() }
    }

    fun positive(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.positiveButton(buttonText) {
            action?.invoke(it)
        }
    }

    fun neutral(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.neutralButton(buttonText) {
            action?.invoke(it)
        }
    }

    fun build(): MaterialDialog =
        dialog
}
