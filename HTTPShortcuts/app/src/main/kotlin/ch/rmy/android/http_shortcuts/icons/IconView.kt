package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled
import ch.rmy.android.http_shortcuts.utils.Animations

class IconView : AppCompatImageView {

    private var icon: ShortcutIcon? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setIcon(icon: ShortcutIcon, animated: Boolean = false) {
        applyIcon(icon, animated)
    }

    private fun applyIcon(icon: ShortcutIcon, animated: Boolean = false) {
        if (icon == this.icon) {
            return
        }
        val uri = icon.getIconURI(context)
        val tint = (icon as? ShortcutIcon.BuiltInIcon)?.tint
        if (this.icon == null || !animated) {
            this.icon = icon
            applyImageURI(uri, tint)
        } else {
            this.icon = icon
            Animations.zoomSwap(this) {
                applyImageURI(uri, tint)
            }
        }
    }

    private fun applyImageURI(uri: Uri, tint: Int?) {
        setImageURI(uri)
        updateTint(tint)
        updateBackground(tint)
    }

    private fun updateTint(tint: Int?) {
        ImageViewCompat.setImageTintList(this, tint?.let { ColorStateList.valueOf(it) })
    }

    private fun updateBackground(tint: Int?) {
        if (requiresBackground(tint)) {
            setBackgroundResource(R.drawable.icon_background)
        } else {
            background = null
        }
    }

    private fun requiresBackground(tint: Int?) = if (context.isDarkThemeEnabled()) {
        tint == Color.BLACK
    } else {
        tint == Color.WHITE
    }

    override fun setImageURI(uri: Uri?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setImageURI(uri)
            return
        }
        val internalIconPrefix = "android.resource://${context.packageName}/"
        val fullUri = uri?.toString()
        if (fullUri?.startsWith(internalIconPrefix) == true) {
            val iconName = fullUri.removePrefix(internalIconPrefix)
            val drawableId = ShortcutIcon.BuiltInIcon(iconName).getDrawableIdentifier(context)
            setImageResource(drawableId)
        } else {
            super.setImageURI(uri)
        }
    }

}
