package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.net.Uri
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.clearBackground

class IconView : AppCompatImageView {

    var iconName: String? = null
        private set

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        iconName = context.resources.getResourceEntryName(resId)
        updateBackground()
    }

    private fun updateBackground() {
        if (requiresBackground()) {
            setBackgroundResource(R.drawable.icon_background)
        } else {
            clearBackground()
        }
    }

    private fun requiresBackground() = iconName?.startsWith("white_") ?: false

    fun setImageURI(uri: Uri, iconName: String?) {
        setImageURI(uri)
        this.iconName = iconName
        updateBackground()
    }

}
