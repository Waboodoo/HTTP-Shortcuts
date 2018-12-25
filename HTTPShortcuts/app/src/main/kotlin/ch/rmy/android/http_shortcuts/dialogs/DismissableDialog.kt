package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

abstract class DismissableDialog(private val context: Context) : Dialog {

    override fun show(): Promise<Unit, Unit, Unit> {
        val deferred = DeferredObject<Unit, Unit, Unit>()
        val shown = MaterialDialog.Builder(context)
            .positiveText(R.string.dialog_ok)
            .customView(R.layout.dismissable_dialog, true)
            .cancelable(false)
            .canceledOnTouchOutside(false)
            .dismissListener {
                if (deferred.isPending) {
                    deferred.resolve(Unit)
                }
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
        if (!shown) {
            deferred.resolve(Unit)
        }
        return deferred.promise()
    }

    protected abstract val message: String

    protected abstract var isPermanentlyDismissed: Boolean

    override fun shouldShow() = !isPermanentlyDismissed


}