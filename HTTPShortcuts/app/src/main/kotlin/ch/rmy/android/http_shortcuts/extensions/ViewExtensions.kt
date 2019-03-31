package ch.rmy.android.http_shortcuts.extensions

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.SimpleTextWatcher
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

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
