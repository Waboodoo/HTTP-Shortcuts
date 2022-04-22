package ch.rmy.android.framework.extensions

import android.content.Context
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.ui.views.PanelButton
import ch.rmy.android.framework.utils.SimpleAnimationListener
import ch.rmy.android.framework.utils.SimpleTextWatcher
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import kotlin.math.min

var ViewBinding.isVisible: Boolean
    get() = root.isVisible
    set(value) {
        root.isVisible = value
    }

val View.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(context)

fun EditText.focus() {
    requestFocus()
    try {
        setSelection(text.length)
    } catch (e: Exception) {
        logException(e)
    }
}

fun EditText.setMaxLength(maxLength: Int) {
    filters = arrayOf(*filters, InputFilter.LengthFilter(maxLength))
}

fun EditText.insertAroundCursor(before: String, after: String = "") {
    val cursor = selectionStart
    val position = if (cursor != -1 && cursor < text.length) {
        cursor
    } else {
        text.length
    }

    text.insert(position, before)
    text.insert(position + before.length, after)
    setSelection(position + before.length)
    showSoftKeyboard()
}

fun View.showSoftKeyboard() {
    requestFocus()
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.addRippleAnimation(borderless: Boolean = false) {
    val attrs = intArrayOf(if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground)
    val typedArray = context.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    typedArray.recycle()
    setBackgroundResource(backgroundResource)
}

val Toolbar.titleView: TextView?
    get() {
        for (i in 0 until childCount) {
            val child = getChildAt(i) as? TextView
            if (child != null) {
                return child
            }
        }
        return null
    }

fun CheckBox.observeChecked(): Observable<Boolean> {
    val subject = PublishSubject.create<Boolean>()
    val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        subject.onNext(isChecked)
    }
    return subject
        .doOnSubscribe {
            setOnCheckedChangeListener(listener)
        }
        .doOnDispose {
            setOnCheckedChangeListener(null)
        }
}

fun EditText.observeTextChanges(): Observable<CharSequence> {
    val subject = PublishSubject.create<CharSequence>()
    val watcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (getTag(R.string.edit_text_suppress_listeners) != true) {
                subject.onNext(s)
            }
        }
    }
    return subject
        .doOnSubscribe {
            addTextChangedListener(watcher)
        }
        .doOnDispose {
            removeTextChangedListener(watcher)
        }
        .throttleLatest(200, TimeUnit.MILLISECONDS, true)
        .observeOn(AndroidSchedulers.mainThread())
}

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

fun TextView.setText(localizable: Localizable?) {
    text = localizable?.localize(context)
}

fun TextView.setHint(localizable: Localizable?) {
    hint = localizable?.localize(context)
}

fun Toolbar.setTitle(localizable: Localizable?) {
    title = localizable?.localize(context)
}

fun Toolbar.setSubtitle(localizable: Localizable?) {
    subtitle = localizable?.localize(context)
}

fun PanelButton.setSubtitle(localizable: Localizable?) {
    subtitle = localizable?.localize(context) ?: ""
}

val RecyclerView.ViewHolder.context: Context
    get() = itemView.context

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

fun View.stopAndRemoveAnimations() {
    animation?.setAnimationListener(null)
    animation?.cancel()
    clearAnimation()
}

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
