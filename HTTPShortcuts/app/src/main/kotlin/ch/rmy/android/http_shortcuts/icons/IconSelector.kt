package ch.rmy.android.http_shortcuts.icons

import android.app.Dialog
import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

class IconSelector(context: Context) {

    private val dialog: Dialog
    private val source = MaybeSubject.create<String>()

    init {
        dialog = MaterialDialog.Builder(context)
            .title(R.string.title_choose_icon)
            .customView(R.layout.dialog_icon_selector, false)
            .dismissListener {
                source.onComplete()
            }
            .build()

        val grid = dialog.findViewById(R.id.icon_selector_grid) as RecyclerView
        grid.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 6)
        grid.layoutManager = layoutManager

        val adapter = IconAdapter(context) { iconName ->
            dialog.dismiss()
            source.onSuccess(iconName)
        }
        grid.adapter = adapter
    }

    fun show(): Maybe<String> {
        val visible = dialog.showIfPossible()
        if (!visible) {
            source.onComplete()
        }
        return source
    }

}
