package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject

abstract class DismissableDialog(private val context: Context, private val isCancelable: Boolean = false) : Dialog {

    override fun show(): Single<DialogResult> {
        var doNotShowAgain = false
        val single = SingleSubject.create<DialogResult>()
        val dialog = DialogBuilder(context)
            .positive(R.string.dialog_ok) {
                isPermanentlyDismissed = doNotShowAgain
                single.onSuccess(DialogResult.OK)
            }
            .view(R.layout.dismissable_dialog)
            .cancelable(isCancelable)
            .canceledOnTouchOutside(isCancelable)
            .dismissListener {
                if (!isCancelable) {
                    isPermanentlyDismissed = doNotShowAgain
                }
                if (!single.hasValue()) {
                    single.onSuccess(DialogResult.CANCELED)
                }
            }
            .mapIf(isCancelable) {
                negative(R.string.dialog_cancel) {
                    single.onSuccess(DialogResult.CANCELED)
                }
            }
            .build()
            .also {
                val messageView = it.findViewById(R.id.dialog_message) as TextView
                messageView.text = message
                val checkBox = it.findViewById(R.id.checkbox_do_not_show_again) as CheckBox
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    doNotShowAgain = isChecked
                }
            }
            .showIfPossible()
        return if (dialog != null) {
            single.doOnDispose {
                dialog.dismiss()
            }
        } else {
            Single.just(DialogResult.NOT_SHOWN)
        }
    }

    protected abstract val message: String

    protected abstract var isPermanentlyDismissed: Boolean

    override fun shouldShow() = !isPermanentlyDismissed
}
