package ch.rmy.android.http_shortcuts.icons

import android.app.Dialog
import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

class IconSelector(context: Context) {

    private val dialog: Dialog
    private val source = MaybeSubject.create<String>()

    init {
        dialog = DialogBuilder(context)
            .title(R.string.title_choose_icon)
            .view(R.layout.dialog_icon_selector)
            .dismissListener {
                source.onComplete()
            }
            .build()
    }

    fun show(): Maybe<String> {
        dialog.showIfPossible()
            ?.let {
                val grid = dialog.findViewById(R.id.icon_selector_grid) as RecyclerView
                grid.setHasFixedSize(true)
                val layoutManager = GridLayoutManager(dialog.context, 6)
                grid.layoutManager = layoutManager

                val adapter = IconAdapter(dialog.context) { iconName ->
                    dialog.dismiss()
                    source.onSuccess(iconName)
                }
                grid.adapter = adapter
            }
            ?: run {
                source.onComplete()
            }
        return source
    }

}
