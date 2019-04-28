package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.math.min

var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        val newState = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
        if (visibility != newState) {
            visibility = newState
        }
    }

fun EditText.focus() {
    requestFocus()
    try {
        setSelection(text.length)
    } catch (e: Exception) {
        logException(e)
    }
}

fun View.showSoftKeyboard() {
    requestFocus()
    post {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

@Suppress("DEPRECATION")
fun ImageView.clearBackground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        background = null
    } else {
        setBackgroundDrawable(null)
    }
}

fun ViewGroup.setContentView(@LayoutRes layoutResource: Int): View =
    View.inflate(context, layoutResource, this)

fun View.addRippleAnimation(borderless: Boolean = false) {
    val attrs = intArrayOf(if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground)
    val typedArray = context.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    typedArray.recycle()
    setBackgroundResource(backgroundResource)
}

fun TextView.observeTextChanges(): Observable<CharSequence> {
    var previousText: CharSequence = text
    val subject = PublishSubject.create<CharSequence>()
    val watcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s != previousText) {
                subject.onNext(s)
            }
            previousText = s
        }
    }
    return subject
        .doOnSubscribe {
            addTextChangedListener(watcher)
        }
        .doOnDispose {
            removeTextChangedListener(watcher)
        }
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

fun EditText.setTextSafely(text: CharSequence) {
    if (isFocused) {
        return
    }
    val start = selectionStart
    val end = selectionEnd
    setText(text)
    if (start != -1 && end != -1) {
        val length = length()
        setSelection(min(start, length), min(end, length))
    }
}