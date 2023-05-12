package ch.rmy.android.http_shortcuts.extensions

import android.content.res.ColorStateList
import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.utils.ThemeHelper

@Deprecated("Sooooon...")
fun CheckBox.applyTheme(themeHelper: ThemeHelper) {
    buttonTintList = ColorStateList.valueOf(themeHelper.getPrimaryColor(context))
}
