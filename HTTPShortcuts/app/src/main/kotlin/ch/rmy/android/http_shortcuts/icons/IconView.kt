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
import ch.rmy.android.http_shortcuts.utils.IconUtil

class IconView : AppCompatImageView {

    var iconName: String? = null
        private set

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setIcon(iconName: String?, animated: Boolean = false) {
        setImageURI(IconUtil.getIconURI(context, iconName), iconName, animated)
    }

    private fun setImageURI(uri: Uri, iconName: String?, animated: Boolean = false) {
        if (this.iconName == iconName) {
            return
        }
        val tint = IconUtil.getIconTint(iconName)
        if (this.iconName == null || !animated) {
            this.iconName = iconName
            applyImageURI(uri, tint)
        } else {
            this.iconName = iconName
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
            val drawableId = IconUtil.getDrawableIdentifier(context, iconName)
            setImageResource(drawableId)
        } else {
            super.setImageURI(uri)
        }
    }

}
