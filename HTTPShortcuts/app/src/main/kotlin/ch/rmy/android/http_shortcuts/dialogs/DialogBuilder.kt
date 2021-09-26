package ch.rmy.android.http_shortcuts.dialogs

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.widget.ImageViewCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import io.reactivex.Completable

open class DialogBuilder(val context: Context) {

    private val dialog = MaterialDialog(context)
    private val items = mutableListOf<MenuItem>()

    fun title(@StringRes title: Int) = also {
        dialog.title(res = title)
    }

    fun title(title: String?) = also {
        if (title?.isNotEmpty() == true) {
            dialog.title(text = title)
        }
    }

    fun item(
        @StringRes nameRes: Int? = null,
        name: CharSequence? = null,
        @StringRes descriptionRes: Int? = null,
        description: CharSequence? = null,
        shortcutIcon: ShortcutIcon? = null,
        @DrawableRes iconRes: Int? = null,
        action: () -> Unit = {},
    ) = also {
        items.add(MenuItem.ClickableItem(
            name ?: context.getString(nameRes!!),
            description ?: (descriptionRes?.let { context.getString(it) }),
            shortcutIcon,
            iconRes,
            action,
        ))
    }

    fun separator() = also {
        items.add(MenuItem.Separator)
    }

    open fun message(@StringRes text: Int, isHtml: Boolean = false) =
        message(context.getString(text), isHtml)

    open fun message(text: CharSequence, isHtml: Boolean = false) = also {
        dialog.message(text = text) {
            messageTextView.movementMethod = LinkMovementMethod.getInstance()
            if (isHtml) {
                html()
            }
        }
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
        dialog.mapIf(items.isNotEmpty()) {
            val listView = (LayoutInflater.from(context).inflate(R.layout.menu_dialog, null, false) as ListView)
                .apply {
                    adapter = MenuListAdapter(context, items, dialog)
                    divider = null
                }

            customView(view = listView)
        }

    fun show() = build().showIfPossible()

    fun showIfPossible() = show()

    fun showAsCompletable(): Completable =
        Completable.create { emitter ->
            dismissListener {
                emitter.onComplete()
            }
                .showIfPossible()
                ?: let {
                    emitter.onComplete()
                }
        }

    private sealed interface MenuItem {

        object Separator : MenuItem

        class ClickableItem(
            val name: CharSequence,
            val description: CharSequence?,
            val shortcutIcon: ShortcutIcon?,
            val iconRes: Int?,
            val action: (() -> Unit)?,
        ) : MenuItem

    }

    private inner class MenuListAdapter(
        context: Context,
        items: List<MenuItem>,
        private val dialog: Dialog,
    ) : ArrayAdapter<MenuItem>(context, 0, items) {

        private val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getItemViewType(position: Int): Int =
            when (getItem(position)!!) {
                is MenuItem.ClickableItem -> TYPE_CLICKABLE_ITEM
                else -> TYPE_SEPARATOR
            }

        override fun getViewTypeCount(): Int = 2

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            when (val item = getItem(position)!!) {
                is MenuItem.ClickableItem -> getClickableItemView(item, convertView, parent)
                is MenuItem.Separator -> getSeparatorView(convertView, parent)
            }

        private fun getClickableItemView(item: MenuItem.ClickableItem, convertView: View?, parent: ViewGroup): View {
            val view: ViewGroup = convertView as? ViewGroup
                ?: layoutInflater.inflate(R.layout.menu_dialog_item, parent, false) as ViewGroup

            val labelView: TextView = view.findViewById(R.id.menu_item_label)
            val descriptionView: TextView = view.findViewById(R.id.menu_item_description)
            val iconContainer: View = view.findViewById(R.id.menu_item_icon_container)
            val shortcutIconView: IconView = view.findViewById(R.id.menu_item_shortcut_icon)
            val regularIconView: ImageView = view.findViewById(R.id.menu_item_regular_icon)

            labelView.text = item.name
            descriptionView.visible = item.description != null
            descriptionView.text = item.description
            when {
                item.shortcutIcon != null -> {
                    shortcutIconView.setIcon(item.shortcutIcon)
                    shortcutIconView.visible = true
                    regularIconView.visible = false
                    iconContainer.visible = true
                }
                item.iconRes != null -> {
                    shortcutIconView.visible = false
                    regularIconView.visible = true
                    iconContainer.visible = true
                    regularIconView.setImageResource(item.iconRes)
                    ImageViewCompat.setImageTintList(regularIconView, ColorStateList.valueOf(color(context, R.color.dialog_icon)))
                }
                else -> {
                    shortcutIconView.visible = false
                    regularIconView.visible = false
                    iconContainer.visible = false
                }
            }

            view.setOnClickListener {
                item.action?.invoke()
                dialog.dismiss()
            }

            return view
        }

        private fun getSeparatorView(convertView: View?, parent: ViewGroup): View =
            convertView ?: layoutInflater.inflate(R.layout.menu_dialog_separator, parent, false)

    }

    companion object {

        private const val TYPE_CLICKABLE_ITEM = 0
        private const val TYPE_SEPARATOR = 1

    }

}
