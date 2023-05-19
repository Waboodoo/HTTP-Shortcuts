package ch.rmy.android.http_shortcuts.utils

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.MenuDialogBinding
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

@Deprecated("Use Compose instead")
class DialogBuilder(val context: Context) {

    private val dialog = MaterialDialog(context)
    private val items = mutableListOf<MenuItem>()

    fun title(title: String?) = also {
        if (title?.isNotEmpty() == true) {
            dialog.title(text = title)
        }
    }

    fun title(title: Localizable?) = also {
        title(title?.localize(context)?.toString())
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
        items.add(
            MenuItem(
                name ?: context.getString(nameRes!!),
                description ?: (descriptionRes?.let { context.getString(it) }),
                shortcutIcon,
                iconRes,
                action,
            )
        )
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

    fun view(@LayoutRes view: Int) = also {
        dialog.customView(viewRes = view)
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

    fun negative(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.negativeButton(buttonText) {
            action?.invoke(it)
        }
    }

    fun neutral(@StringRes buttonText: Int, action: ((MaterialDialog) -> Unit)? = null) = also {
        dialog.neutralButton(buttonText) {
            action?.invoke(it)
        }
    }

    fun build(): MaterialDialog =
        dialog.runIf(items.isNotEmpty()) {
            val listView = MenuDialogBinding.inflate(LayoutInflater.from(context)).root
                .apply {
                    adapter = MenuListAdapter(this@DialogBuilder.context, items, dialog)
                    divider = null
                }
            listView.itemsCanFocus = true

            customView(view = listView)
        }

    fun show() = build().show()

    class MenuItem(
        val name: CharSequence,
        val description: CharSequence?,
        val shortcutIcon: ShortcutIcon?,
        val iconRes: Int?,
        val action: (() -> Unit)?,
    )

    private inner class MenuListAdapter(
        context: Context,
        items: List<MenuItem>,
        private val dialog: Dialog,
    ) : ArrayAdapter<MenuItem>(context, 0, items) {

        private val layoutInflater: LayoutInflater = context.getSystemService()!!

        override fun getItemViewType(position: Int): Int =
            0

        override fun getViewTypeCount(): Int = 2

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            getClickableItemView(getItem(position)!!, convertView, parent)

        private fun getClickableItemView(item: MenuItem, convertView: View?, parent: ViewGroup): View {
            val view: ViewGroup = convertView as? ViewGroup
                ?: layoutInflater.inflate(R.layout.menu_dialog_item, parent, false) as ViewGroup

            val labelView: TextView = view.findViewById(R.id.menu_item_label)
            val descriptionView: TextView = view.findViewById(R.id.menu_item_description)
            val iconContainer: View = view.findViewById(R.id.menu_item_icon_container)
            val shortcutIconView: IconView = view.findViewById(R.id.menu_item_shortcut_icon)
            val regularIconView: ImageView = view.findViewById(R.id.menu_item_regular_icon)

            labelView.text = item.name
            descriptionView.isVisible = item.description != null
            descriptionView.text = item.description
            when {
                item.shortcutIcon != null -> {
                    shortcutIconView.setIcon(item.shortcutIcon)
                    shortcutIconView.isVisible = true
                    regularIconView.isVisible = false
                    iconContainer.isVisible = true
                }
                item.iconRes != null -> {
                    shortcutIconView.isVisible = false
                    regularIconView.isVisible = true
                    iconContainer.isVisible = true
                    regularIconView.setImageResource(item.iconRes)
                    ImageViewCompat.setImageTintList(regularIconView, ColorStateList.valueOf(color(context, R.color.dialog_icon)))
                }
                else -> {
                    shortcutIconView.isVisible = false
                    regularIconView.isVisible = false
                    iconContainer.isVisible = false
                }
            }

            view.setOnClickListener {
                item.action?.invoke()
                dialog.dismiss()
            }

            return view
        }
    }
}
