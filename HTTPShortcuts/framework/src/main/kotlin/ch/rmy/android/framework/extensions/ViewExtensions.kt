package ch.rmy.android.framework.extensions

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.content.res.use
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.R
import ch.rmy.android.framework.ui.views.PanelButton
import ch.rmy.android.framework.utils.SimpleAnimationListener
import ch.rmy.android.framework.utils.localization.Localizable
import kotlin.math.min

@Deprecated("Will be removed once fully migrated to Compose")
var ViewBinding.isVisible: Boolean
    get() = root.isVisible
    set(value) {
        root.isVisible = value
    }

@Deprecated("Will be removed once fully migrated to Compose")
val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

@Deprecated("Will be removed once fully migrated to Compose")
fun EditText.focus() {
    requestFocus()
    try {
        setSelection(text.length)
    } catch (e: Exception) {
        logException(e)
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(*filters, InputFilter.LengthFilter(maxLength))
}

@Deprecated("Will be removed once fully migrated to Compose")
fun View.showSoftKeyboard() {
    requestFocus()
    post {
        context.getSystemService<InputMethodManager>()
            ?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
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
fun CheckBox.doOnCheckedChanged(onCheckedChanged: (checked: Boolean) -> Unit) {
    setOnCheckedChangeListener { _, isChecked ->
        onCheckedChanged(isChecked)
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
fun EditText.doOnTextChanged(onTextChanged: (text: CharSequence) -> Unit) {
    var onCooldown = false
    var value: CharSequence? = null
    val handler = Handler(Looper.getMainLooper())
    val runnable = Runnable {
        value?.let(onTextChanged)
        onCooldown = false
    }
    doOnTextChanged { s, _, _, _ ->
        if (getTag(R.string.edit_text_suppress_listeners) != true) {
            if (!onCooldown) {
                onTextChanged(s ?: "")
                value = null
                onCooldown = true
            } else {
                value = s
            }
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 200)
        }
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
fun EditText.setTextSafely(text: CharSequence) {
    if ((isFocused && this.text.isNotEmpty()) || this.text.toString() == text.toString()) {
        return
    }
    try {
        setTag(R.string.edit_text_suppress_listeners, true)
        val wasEmpty = this.text.isEmpty()
        val start = selectionStart
        val end = selectionEnd
        setText(text)
        if (start != -1 && end != -1) {
            val length = length()
            if (wasEmpty && start == 0 && end == 0) {
                setSelection(length, length)
            } else {
                setSelection(min(start, length), min(end, length))
            }
        }
    } finally {
        setTag(R.string.edit_text_suppress_listeners, false)
    }
}

@Deprecated("Will be removed once fully migrated to Compose")
fun TextView.setText(localizable: Localizable?) {
    text = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun TextView.setHint(localizable: Localizable?) {
    hint = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun Toolbar.setTitle(localizable: Localizable?) {
    title = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun Toolbar.setSubtitle(localizable: Localizable?) {
    subtitle = localizable?.localize(context)
}

@Deprecated("Will be removed once fully migrated to Compose")
fun PanelButton.setSubtitle(localizable: Localizable?) {
    subtitle = localizable?.localize(context) ?: ""
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
