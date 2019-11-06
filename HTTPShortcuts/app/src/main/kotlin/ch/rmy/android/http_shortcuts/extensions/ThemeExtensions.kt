package ch.rmy.android.http_shortcuts.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.preference.Preference
import ch.rmy.android.http_shortcuts.utils.ThemeHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

fun TabLayout.applyTheme(themeHelper: ThemeHelper) {
    setTabTextColors(Color.WHITE, Color.WHITE)
    setSelectedTabIndicatorColor(Color.WHITE)
    setBackgroundColor(themeHelper.getPrimaryColor(context))
}

fun FloatingActionButton.applyTheme(themeHelper: ThemeHelper) {
    drawable?.setTint(Color.WHITE)
    backgroundTintList = ColorStateList.valueOf(themeHelper.getPrimaryColor(context))
}

fun Preference.applyTheme(themeHelper: ThemeHelper) {
    if (themeHelper.isDarkThemeEnabled) {
        icon?.setTint(Color.WHITE)
    }
}