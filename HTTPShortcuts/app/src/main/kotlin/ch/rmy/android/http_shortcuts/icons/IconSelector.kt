package ch.rmy.android.http_shortcuts.icons

import android.app.Dialog
import android.content.Context
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.showIfPossible
import ch.rmy.android.http_shortcuts.extensions.showToast
import io.reactivex.Maybe
import io.reactivex.subjects.MaybeSubject

abstract class IconSelector(context: Context, @StringRes title: Int) {

    private val dialog: Dialog
    private val source = MaybeSubject.create<ShortcutIcon>()

    init {
        dialog = DialogBuilder(context)
            .title(title)
            .view(R.layout.dialog_icon_selector)
            .dismissListener {
                source.onComplete()
            }
            .build()
    }

    fun show(): Maybe<ShortcutIcon> {
        val icons = getIcons()
        if (icons.isEmpty()) {
            dialog.context.showToast(R.string.error_no_custom_icons)
            return Maybe.empty()
        }

        dialog.showIfPossible()
            ?.let {
                val grid = dialog.findViewById(R.id.icon_selector_grid) as RecyclerView
                grid.setHasFixedSize(true)
                val layoutManager = GridLayoutManager(dialog.context, 6)
                grid.layoutManager = layoutManager

                val adapter = IconAdapter(icons) { icon ->
                    dialog.dismiss()
                    source.onSuccess(icon)
                }
                grid.adapter = adapter
            }
            ?: run {
                source.onComplete()
            }
        return source
    }

    protected abstract fun getIcons(): List<ShortcutIcon>
}
