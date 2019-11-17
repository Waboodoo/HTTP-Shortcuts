package ch.rmy.android.http_shortcuts.extensions

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.setTintCompat(@ColorInt color: Int) {
    DrawableCompat.setTint(this, color)
}