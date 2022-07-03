package ch.rmy.android.framework.ui.views

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import ch.rmy.android.framework.extensions.logException

class ResilientEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle,
) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean =
        try {
            super.dispatchTouchEvent(event)
        } catch (e: IndexOutOfBoundsException) {
            true
        } catch (e: IllegalStateException) {
            true
        }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? =
        super.onCreateInputConnection(outAttrs)
            ?.let {
                object : InputConnectionWrapper(it, false) {
                    override fun getSelectedText(flags: Int): CharSequence? =
                        try {
                            super.getSelectedText(flags)
                        } catch (e: IndexOutOfBoundsException) {
                            null
                        }
                }
            }

    override fun onDraw(canvas: Canvas?) {
        try {
            super.onDraw(canvas)
        } catch (e: ArrayIndexOutOfBoundsException) {
            logException(e)
        }
    }
}
