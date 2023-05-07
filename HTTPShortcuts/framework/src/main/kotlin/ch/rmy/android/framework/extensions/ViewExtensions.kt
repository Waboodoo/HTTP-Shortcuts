package ch.rmy.android.framework.extensions

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.R
import ch.rmy.android.framework.utils.SimpleAnimationListener
import ch.rmy.android.framework.utils.localization.Localizable

@Deprecated("Will be removed once fully migrated to Compose")
var ViewBinding.isVisible: Boolean
    get() = root.isVisible
    set(value) {
        root.isVisible = value
    }

@Deprecated("Will be removed once fully migrated to Compose")
fun View.addRippleAnimation(borderless: Boolean = false) {
    val attrs = intArrayOf(if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground)
    context.obtainStyledAttributes(attrs).use { typedArray ->
        val backgroundResource = typedArray.getResourceId(0, 0)
        setBackgroundResource(backgroundResource)
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
val Toolbar.titleView: TextView?
    get() = children
        .filterIsInstance<TextView>()
        .firstOrNull()

@Deprecated("Will be removed once fully migrated to Compose")
fun TextView.setText(localizable: Localizable?) {
    text = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun Toolbar.setTitle(localizable: Localizable?) {
    title = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
val RecyclerView.ViewHolder.context: Context
    get() = itemView.context

@Deprecated("Will be removed once fully migrated to Compose")
fun View.zoomSwap(action: () -> Unit) {
    stopAndRemoveAnimations()
    val zoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
    zoomOut.setAnimationListener(object : SimpleAnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            action.invoke()
            val zoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)
            startAnimation(zoomIn)
        }
    })
    startAnimation(zoomOut)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun View.stopAndRemoveAnimations() {
    animation?.setAnimationListener(null)
    animation?.cancel()
    clearAnimation()
}

@Deprecated("Will be removed once fully migrated to Compose")
fun View.zoomToggle(visible: Boolean) {
    val zoomToggleState = (getTag(R.string.animation_zoom_toggle) as? Boolean) ?: isVisible
    if (!visible && zoomToggleState) {
        setTag(R.string.animation_zoom_toggle, false)
        stopAndRemoveAnimations()
        val zoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
        zoomOut.setAnimationListener(object : SimpleAnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                isVisible = false
            }
        })
        startAnimation(zoomOut)
    } else if (visible && !zoomToggleState) {
        setTag(R.string.animation_zoom_toggle, true)
        stopAndRemoveAnimations()
        val zoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)
        isVisible = true
        zoomIn.setAnimationListener(object : SimpleAnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isVisible = true
            }
        })
        startAnimation(zoomIn)
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
fun LifecycleOwner.doOnDestroy(action: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            action()
        }
    })
}
