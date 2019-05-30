package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog

class MenuDialogBuilder(val context: Context) {

    private val builder = MaterialDialog.Builder(context)
    private val names = mutableListOf<CharSequence>()
    private val actions = mutableListOf<() -> Unit>()

    fun title(@StringRes title: Int) = also {
        it.title(builder.context.getString(title))
    }

    fun title(title: CharSequence) = also { builder.title(title) }

    fun item(@StringRes name: Int, action: () -> Unit) = also {
        item(builder.context.getString(name), action)
    }

    fun item(name: CharSequence, action: () -> Unit) = also {
        names.add(name)
        actions.add(action)
    }

    fun dismissListener(onDismissListener: () -> Unit) = also {
        builder.dismissListener { onDismissListener() }
    }

    fun toDialogBuilder() =
        builder.mapIf(names.isNotEmpty()) {
            builder
                .items(names)
                .itemsCallback { _, _, which, _ ->
                    actions[which]()
                }!!
        }

    fun show() = toDialogBuilder().showIfPossible()

}
