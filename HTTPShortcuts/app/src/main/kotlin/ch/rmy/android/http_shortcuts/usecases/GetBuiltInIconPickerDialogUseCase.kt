package ch.rmy.android.http_shortcuts.usecases

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.BuiltInIconAdapter
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.GridLayoutManager

class GetBuiltInIconPickerDialogUseCase {

    operator fun invoke(onIconSelected: (ShortcutIcon.BuiltInIcon) -> Unit): DialogState =
        DialogState.create(DIALOG_ID) {
            title(R.string.title_choose_icon)
                .view(R.layout.dialog_icon_selector)
                .build()
                .show {
                    val grid = findViewById<RecyclerView>(R.id.icon_selector_grid)
                    grid.setHasFixedSize(true)
                    val layoutManager = GridLayoutManager(context, R.dimen.grid_layout_icon_width)
                    grid.layoutManager = layoutManager

                    val adapter = BuiltInIconAdapter(getIcons(context)) { icon ->
                        dismiss()
                        onIconSelected(icon)
                    }
                    grid.adapter = adapter
                }
        }

    private fun getIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        getNormalIcons(context).plus(getTintedIcons(context))

    private fun getNormalIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        Icons.ICONS
            .map {
                ShortcutIcon.BuiltInIcon.fromDrawableResource(context, it)
            }

    private fun getTintedIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
        Icons.TintColors.values()
            .flatMap { tint ->
                Icons.TINTABLE_ICONS.map { iconResource ->
                    ShortcutIcon.BuiltInIcon.fromDrawableResource(context, iconResource, tint)
                }
            }

    companion object {
        private const val DIALOG_ID = "built-in-icon-picker"
    }
}
