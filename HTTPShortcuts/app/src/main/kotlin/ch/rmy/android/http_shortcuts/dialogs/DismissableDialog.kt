package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject

abstract class DismissableDialog(private val context: Context) : Dialog {

    override fun show(): Completable {
        val completable = CompletableSubject.create()
        val dialog = MaterialDialog.Builder(context)
            .positiveText(R.string.dialog_ok)
            .customView(R.layout.dismissable_dialog, true)
            .cancelable(false)
            .canceledOnTouchOutside(false)
            .dismissListener {
                completable.onComplete()
            }
            .build()
            .also {
                val messageView = it.findViewById(R.id.dialog_message) as TextView
                messageView.text = message
                val checkBox = it.findViewById(R.id.checkbox_do_not_show_again) as CheckBox
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    isPermanentlyDismissed = isChecked
                }
            }
            .showIfPossible()
        return if (dialog != null) {
            completable.doOnDispose {
                dialog.dismiss()
            }
        } else {
            Completable.complete()
        }
    }

    protected abstract val message: String

    protected abstract var isPermanentlyDismissed: Boolean

    override fun shouldShow() = !isPermanentlyDismissed


}