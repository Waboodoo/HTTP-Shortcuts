package ch.rmy.android.http_shortcuts.icons

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog

class IconSelector(context: Context, listener: (String) -> Unit) {

    private val dialog: Dialog

    init {
        dialog = MaterialDialog.Builder(context)
                .title(R.string.title_choose_icon)
                .customView(R.layout.dialog_icon_selector, false)
                .build()

        val grid = dialog.findViewById(R.id.icon_selector_grid) as RecyclerView
        grid.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 6)
        grid.layoutManager = layoutManager
        val adapter = IconAdapter(context) { iconName ->
            dialog.dismiss()
            listener(iconName)
        }
        grid.adapter = adapter
    }

    fun show() {
        dialog.showIfPossible()
    }

}
