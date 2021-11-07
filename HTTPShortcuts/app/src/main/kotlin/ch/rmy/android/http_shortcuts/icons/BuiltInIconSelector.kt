package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import ch.rmy.android.http_shortcuts.R

class BuiltInIconSelector(context: Context) : IconSelector(context, R.string.title_choose_icon) {

    private val normalIcons: List<ShortcutIcon.BuiltInIcon> =
        Icons.ICONS
            .map {
                ShortcutIcon.BuiltInIcon.fromDrawableResource(context, it)
            }

    private val tintedIcons: List<ShortcutIcon.BuiltInIcon> =
        Icons.TintColors.values()
            .flatMap { tint ->
                Icons.TINTABLE_ICONS.map { iconResource ->
                    ShortcutIcon.BuiltInIcon.fromDrawableResource(context, iconResource, tint)
                }
            }

    override fun getIcons(): List<ShortcutIcon> =
        normalIcons.plus(tintedIcons)

}