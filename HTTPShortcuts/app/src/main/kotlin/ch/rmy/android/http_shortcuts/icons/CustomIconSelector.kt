package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.IconUtil

class CustomIconSelector(private val context: Context) : IconSelector(context, R.string.title_choose_previously_used_icon) {

    override fun getIcons(): List<ShortcutIcon> =
        IconUtil.getCustomIconNamesInApp(context)
            .map {
                ShortcutIcon.CustomIcon(it)
            }

}