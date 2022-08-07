package ch.rmy.android.http_shortcuts.usecases

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.BuiltInIconAdapter
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager
import javax.inject.Inject

class GetBuiltInIconPickerDialogUseCase
@Inject
constructor() {

    operator fun invoke(onIconSelected: (ShortcutIcon.BuiltInIcon) -> Unit): DialogState =
        DialogState.create(DIALOG_ID) {
            title(R.string.title_choose_icon)
                .view(R.layout.dialog_icon_selector)
                .build()
                .show {
                    val grid = findViewById<RecyclerView>(R.id.icon_selector_grid)
                    grid.setHasFixedSize(true)
                    val layoutManager = GridLayoutManager(context, R.dimen.grid_layout_builtin_icon_width)
                    grid.layoutManager = layoutManager
                    view.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
                        layoutManager.setTotalWidth(view.width)
                    }
                    val adapter = BuiltInIconAdapter(getIcons(context)) { icon ->
                        dismiss()
                        onIconSelected(icon)
                    }
                    grid.adapter = adapter
                }
        }

    private fun getIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        getColoredIcons(context).plus(getTintableIcons(context))

    private fun getColoredIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        Icons.getColoredIcons()
            .map {
                ShortcutIcon.BuiltInIcon.fromDrawableResource(context, it)
            }

    private fun getTintableIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        Icons.getTintableIcons().map { iconResource ->
            ShortcutIcon.BuiltInIcon.fromDrawableResource(context, iconResource, Icons.TintColors.BLACK)
        }

    companion object {
        private const val DIALOG_ID = "built-in-icon-picker"
    }
}
