package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import com.afollestad.materialdialogs.MaterialDialog

class MenuDialogBuilder(val context: Context) {

    private val builder = MaterialDialog.Builder(context)
    private val names = mutableListOf<CharSequence>()
    private val actions = mutableListOf<() -> Unit>()

    fun title(@StringRes title: Int) = this.also {
        it.title(builder.context.getString(title))
    }

    fun title(title: CharSequence) = this.also { builder.title(title) }

    fun item(@StringRes name: Int, action: () -> Unit) = this.also {
        item(builder.context.getString(name), action)
    }

    fun item(name: CharSequence, action: () -> Unit) = this.also {
        names.add(name)
        actions.add(action)
    }

    fun dismissListener(onDismissListener: () -> Unit) = this.also {
        builder.dismissListener({ onDismissListener() })
    }

    fun toDialogBuilder() = if (names.isEmpty()) {
        builder
    } else {
        builder
                .items(names)
                .itemsCallback { _, _, which, _ ->
                    actions[which]()
                }!!
    }

    fun show() {
        if ((context as? Activity)?.isFinishing == true) {
            return
        }
        toDialogBuilder()
                .show()
    }

}
