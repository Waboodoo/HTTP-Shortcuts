package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.text.InputType
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems

open class DialogBuilder(val context: Context) {

    private val dialog = MaterialDialog(context)
    private val names = mutableListOf<CharSequence>()
    private val actions = mutableListOf<() -> Unit>()

    fun title(@StringRes title: Int) = also {
        dialog.title(res = title)
    }

    fun title(title: String?) = also {
        if (title?.isNotEmpty() == true) {
            dialog.title(text = title)
        }
    }

    fun item(@StringRes name: Int, action: () -> Unit) =
        item(context.getString(name), action)

    fun item(name: CharSequence, action: () -> Unit) = also {
        names.add(name)
        actions.add(action)
    }

    open fun message(@StringRes text: Int) =
        message(context.getString(text))

    open fun message(text: CharSequence) = also {
        dialog.message(text = text)
    }

    fun view(@LayoutRes view: Int) = also {
        dialog.customView(viewRes = view)
    }

    fun view(view: View) = also {
        dialog.customView(view = view)
    }

    fun dismissListener(onDismissListener: () -> Unit) = also {
        dialog.setOnDismissListener { onDismissListener() }
    }

    fun positive(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) =
        positive(context.getString(buttonText), action)

    fun positive(buttonText: String, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.positiveButton(text = buttonText, click = {
            action?.invoke(it)
        })
    }

    fun negative(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) =
        negative(context.getString(buttonText), action)

    fun negative(buttonText: String, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.negativeButton(text = buttonText, click = {
            action?.invoke(it)
        })
    }

    fun neutral(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) =
        neutral(context.getString(buttonText), action)

    fun neutral(buttonText: String, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.neutralButton(text = buttonText, click = {
            action?.invoke(it)
        })
    }

    fun textInput(prefill: String = "", hint: String = "", allowEmpty: Boolean = true, maxLength: Int? = null, inputType: Int = InputType.TYPE_CLASS_TEXT, callback: (String) -> Unit) = also {
        dialog.input(
            hint = hint,
            prefill = prefill,
            allowEmpty = allowEmpty,
            maxLength = maxLength,
            inputType = inputType
        ) { _, text -> callback(text.toString()) }
    }

    fun canceledOnTouchOutside(cancelable: Boolean) = also {
        dialog.cancelOnTouchOutside(cancelable)
    }

    fun cancelable(cancelable: Boolean) = also {
        dialog.cancelable(cancelable)
    }

    fun build(): MaterialDialog =
        dialog.mapIf(names.isNotEmpty()) {
            dialog
                .listItems(
                    items = names,
                    selection = { dialog, index, _ ->
                        actions[index]()
                        dialog.dismiss()
                    }
                )
        }

    fun show() = build().showIfPossible()

    fun showIfPossible() = show()

}
