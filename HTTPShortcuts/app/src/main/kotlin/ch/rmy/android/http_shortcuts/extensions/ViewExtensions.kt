package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
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

fun ViewGroup.setContentView(@LayoutRes layoutResource: Int): View =
    View.inflate(context, layoutResource, this)

fun View.addRippleAnimation(borderless: Boolean = false) {
    val attrs = intArrayOf(if (borderless) R.attr.selectableItemBackgroundBorderless else R.attr.selectableItemBackground)
    val typedArray = context.obtainStyledAttributes(attrs)
    val backgroundResource = typedArray.getResourceId(0, 0)
    typedArray.recycle()
    setBackgroundResource(backgroundResource)
}

fun EditText.observeTextChanges(): Observable<CharSequence> {
    val subject = PublishSubject.create<CharSequence>()
    val watcher = object : SimpleTextWatcher() {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            subject.onNext(s)
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