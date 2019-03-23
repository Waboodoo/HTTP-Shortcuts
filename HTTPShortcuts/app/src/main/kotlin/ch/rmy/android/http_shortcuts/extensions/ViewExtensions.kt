package ch.rmy.android.http_shortcuts.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import ch.rmy.android.http_shortcuts.R

fun ViewGroup.setContentView(@LayoutRes layoutResource: Int): View =
    View.inflate(context, layoutResource, this)

fun View.addRippleAnimation(borderless: Boolean = false) {
    val attrs = intArrayOf(if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground)
    val typedArray = context.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    typedArray.recycle()
    setBackgroundResource(backgroundResource)
}