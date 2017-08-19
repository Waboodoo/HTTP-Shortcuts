package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.DialogInterface
import android.support.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog
import java.util.*

class MenuDialogBuilder(context: Context) {

    private val builder: MaterialDialog.Builder = MaterialDialog.Builder(context)
    private val names = ArrayList<CharSequence>()
    private val actions = ArrayList<() -> Unit>()

    fun title(@StringRes title: Int): MenuDialogBuilder {
        builder.title(builder.context.getString(title))
        return this
    }

    fun title(title: CharSequence): MenuDialogBuilder {
        builder.title(title)
        return this
    }

    fun item(@StringRes name: Int, action: () -> Unit): MenuDialogBuilder {
        return item(builder.context.getString(name), action)
    }

    fun item(name: CharSequence, action: () -> Unit): MenuDialogBuilder {
        names.add(name)
        actions.add(action)
        return this
    }

    fun dismissListener(onDismissListener: DialogInterface.OnDismissListener): MenuDialogBuilder {
        builder.dismissListener(onDismissListener)
        return this
    }

    fun show() {
        builder.items(names).itemsCallback { _, _, which, _ ->
            actions[which]()
        }.show()
    }

}
