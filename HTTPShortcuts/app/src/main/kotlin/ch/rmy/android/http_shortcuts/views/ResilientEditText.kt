package ch.rmy.android.http_shortcuts.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText

class ResilientEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean =
        try {
            super.dispatchTouchEvent(event)
        } catch (e: IndexOutOfBoundsException) {
            true
        }

}