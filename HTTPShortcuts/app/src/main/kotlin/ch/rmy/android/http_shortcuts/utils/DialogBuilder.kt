package ch.rmy.android.http_shortcuts.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.MenuDialogBinding
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.getActionButton
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input

class DialogBuilder(val context: Context) {

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
            MenuItem.ClickableItem(
                name ?: context.getString(nameRes!!),
                description ?: (descriptionRes?.let { context.getString(it) }),
                shortcutIcon,
                iconRes,
                action,
            )
        )
    }

    fun checkBoxItem(
        @StringRes nameRes: Int? = null,
        name: CharSequence? = null,
        @StringRes descriptionRes: Int? = null,
        description: CharSequence? = null,
        shortcutIcon: ShortcutIcon? = null,
        checked: () -> Boolean,
        action: (Boolean) -> Unit = {},
    ) = also {
        items.add(
            MenuItem.CheckBoxItem(
                name ?: context.getString(nameRes!!),
                description ?: (descriptionRes?.let { context.getString(it) }),
                shortcutIcon,
                checked,
                action,
            )
        )
    }

    fun message(text: Localizable) =
        message(text.localize(context))

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

    @SuppressLint("CheckResult")
    fun textInput(
        prefill: String = "",
        hint: String = "",
        allowEmpty: Boolean = true,
        maxLength: Int? = null,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        callback: (String) -> Unit,
    ) = also {
        dialog.input(
            hint = hint,
            prefill = prefill,
            allowEmpty = allowEmpty,
            maxLength = maxLength,
            inputType = inputType,
        ) { _, text -> callback(text.toString()) }

        dialog.getInputField()
            .apply {
                setOnKeyListener { _, keyCode, _ ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE == 0)) consume {
                        dialog.getActionButton(WhichButton.POSITIVE).performClick()
                    } else false
                }
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) consume {
                        dialog.getActionButton(WhichButton.POSITIVE).performClick()
                    } else false
                }
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

    private sealed interface MenuItem {

        class ClickableItem(
            val name: CharSequence,
            val description: CharSequence?,
            val shortcutIcon: ShortcutIcon?,
            val iconRes: Int?,
            val action: (() -> Unit)?,
        ) : MenuItem

        class CheckBoxItem(
            val name: CharSequence,
            val description: CharSequence?,
            val shortcutIcon: ShortcutIcon?,
            val checked: () -> Boolean,
            val action: ((Boolean) -> Unit),
        ) : MenuItem
    }

    private inner class MenuListAdapter(
        context: Context,
        items: List<MenuItem>,
        private val dialog: Dialog,
    ) : ArrayAdapter<MenuItem>(context, 0, items) {

        private val layoutInflater: LayoutInflater = context.getSystemService()!!

        override fun getItemViewType(position: Int): Int =
            TYPE_CLICKABLE_ITEM

        override fun getViewTypeCount(): Int = 2

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
            when (val item = getItem(position)!!) {
                is MenuItem.ClickableItem -> getClickableItemView(item, convertView, parent)
                is MenuItem.CheckBoxItem -> getCheckBoxItemView(item, convertView, parent)
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

        private fun getCheckBoxItemView(item: MenuItem.CheckBoxItem, convertView: View?, parent: ViewGroup): View {
            val view: ViewGroup = convertView as? ViewGroup
                ?: layoutInflater.inflate(R.layout.menu_dialog_checkbox_item, parent, false) as ViewGroup

            val labelView: TextView = view.findViewById(R.id.menu_item_label)
            val descriptionView: TextView = view.findViewById(R.id.menu_item_description)
            val shortcutIconView: IconView = view.findViewById(R.id.menu_item_shortcut_icon)
            val checkBox: CheckBox = view.findViewById(R.id.menu_item_checkbox)

            // Reset the listener to avoid calling the previous one in case the view was recycled
            checkBox.setOnCheckedChangeListener(null)

            labelView.text = item.name
            descriptionView.isVisible = item.description != null
            descriptionView.text = item.description
            checkBox.isChecked = item.checked()
            checkBox.applyTheme()

            when {
                item.shortcutIcon != null -> {
                    shortcutIconView.setIcon(item.shortcutIcon)
                    shortcutIconView.isVisible = true
                }
                else -> {
                    shortcutIconView.isVisible = false
                }
            }

            view.setOnClickListener {
                checkBox.toggle()
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.action.invoke(isChecked)
            }
            return view
        }

        override fun areAllItemsEnabled(): Boolean =
            false

        override fun isEnabled(position: Int): Boolean =
            when (getItemViewType(position)) {
                TYPE_CLICKABLE_ITEM -> true
                else -> false
            }
    }

    private fun CheckBox.applyTheme() {
        buttonTintList = ColorStateList.valueOf(
            if (context.isDarkThemeEnabled()) {
                color(context, R.color.primary_color)
            } else {
                context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).use { attributes ->
                    attributes.getColor(0, color(context, R.color.primary))
                }
            }
        )
    }


    companion object {

        private const val TYPE_CLICKABLE_ITEM = 0
    }
}
