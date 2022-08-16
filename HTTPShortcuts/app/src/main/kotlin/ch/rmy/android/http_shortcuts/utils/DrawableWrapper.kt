package ch.rmy.android.http_shortcuts.utils

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

class DrawableWrapper(var wrappedDrawable: Drawable) : Drawable() {

    override fun draw(canvas: Canvas) {
        wrappedDrawable.draw(canvas)
    }

    override fun onBoundsChange(bounds: Rect) {
        wrappedDrawable.bounds = bounds
    }

    override fun setChangingConfigurations(configs: Int) {
        wrappedDrawable.changingConfigurations = configs
    }

    override fun getChangingConfigurations(): Int =
        wrappedDrawable.changingConfigurations

    @Suppress("DEPRECATION")
    override fun setDither(dither: Boolean) {
        wrappedDrawable.setDither(dither)
    }

    override fun setFilterBitmap(filter: Boolean) {
        wrappedDrawable.isFilterBitmap = filter
    }

    override fun setAlpha(alpha: Int) {
        wrappedDrawable.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        wrappedDrawable.colorFilter = cf
    }

    override fun isStateful(): Boolean =
        wrappedDrawable.isStateful

    override fun setState(stateSet: IntArray): Boolean =
        wrappedDrawable.setState(stateSet)

    override fun getState(): IntArray =
        wrappedDrawable.state

    override fun jumpToCurrentState() {
        wrappedDrawable.jumpToCurrentState()
    }

    override fun getCurrent(): Drawable =
        wrappedDrawable.current

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        super.setVisible(visible, restart) || wrappedDrawable.setVisible(visible, restart)

    @Suppress("DEPRECATION")
    override fun getOpacity(): Int =
        wrappedDrawable.opacity

    override fun getTransparentRegion(): Region? =
        wrappedDrawable.transparentRegion

    override fun getIntrinsicWidth(): Int =
        wrappedDrawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int =
        wrappedDrawable.intrinsicHeight

    override fun getMinimumWidth(): Int =
        wrappedDrawable.minimumWidth

    override fun getMinimumHeight(): Int =
        wrappedDrawable.minimumHeight

    override fun getPadding(padding: Rect): Boolean =
        wrappedDrawable.getPadding(padding)

    override fun onLevelChange(level: Int): Boolean =
        wrappedDrawable.setLevel(level)

    override fun setAutoMirrored(mirrored: Boolean) {
        DrawableCompat.setAutoMirrored(wrappedDrawable, mirrored)
    }

    override fun isAutoMirrored(): Boolean =
        DrawableCompat.isAutoMirrored(wrappedDrawable)

    override fun setTint(tint: Int) {
        DrawableCompat.setTint(wrappedDrawable, tint)
    }

    override fun setTintList(tint: ColorStateList?) {
        DrawableCompat.setTintList(wrappedDrawable, tint)
    }

    override fun setTintMode(tintMode: PorterDuff.Mode?) {
        DrawableCompat.setTintMode(wrappedDrawable, tintMode)
    }

    override fun setHotspot(x: Float, y: Float) {
        DrawableCompat.setHotspot(wrappedDrawable, x, y)
    }

    override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) {
        DrawableCompat.setHotspotBounds(wrappedDrawable, left, top, right, bottom)
    }
}
