package ch.rmy.android.http_shortcuts.icons

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import com.afollestad.materialdialogs.MaterialDialog

/**
 * A dialog window that lists all built-in icons, from which the user can select one.

 * @author Roland Meyer
 */
class IconSelector
/**
 * Creates the icon selection dialog.

 * @param context  The context
 * *
 * @param listener Used as callback when the user selects an icon.
 */
(context: Context, listener: (String) -> Unit) {

    private val dialog: Dialog

    init {
        dialog = MaterialDialog.Builder(context)
                .title(R.string.choose_icon)
                .customView(R.layout.dialog_icon_selector, false)
                .build()

        val grid = dialog.findViewById(R.id.icon_selector_grid) as RecyclerView
        grid.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 6)
        grid.layoutManager = layoutManager
        val adapter = IconAdapter(context, { iconName ->
            dialog.dismiss()
            listener(iconName)
        })
        grid.adapter = adapter
    }

    fun show() {
        dialog.show()
    }

}
