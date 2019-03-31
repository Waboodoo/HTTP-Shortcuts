package ch.rmy.android.http_shortcuts.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import kotlin.math.min

class BetterEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.editTextStyle) : AppCompatEditText(context, attrs, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        val start = selectionStart
        val end = selectionEnd
        super.setText(text, type)
        if (start != -1 && end != -1) {
            setSelection(min(start, length()), min(end, length()))
        }
    }

}