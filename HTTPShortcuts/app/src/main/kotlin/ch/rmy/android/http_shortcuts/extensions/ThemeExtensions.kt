package ch.rmy.android.http_shortcuts.extensions

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ImageView
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
    drawable?.setTintCompat(Color.WHITE)
    backgroundTintList = ColorStateList.valueOf(themeHelper.getPrimaryColor(context))
}

fun Preference.applyTheme() {
    if (context.isDarkThemeEnabled()) {
        icon?.setTintCompat(Color.WHITE)
    }
}

fun ImageView.applyTheme() {
    if (context.isDarkThemeEnabled()) {
        drawable.setTintCompat(Color.WHITE)
    }
}