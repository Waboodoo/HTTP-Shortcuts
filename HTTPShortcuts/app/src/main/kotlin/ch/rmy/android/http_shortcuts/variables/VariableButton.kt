package ch.rmy.android.http_shortcuts.variables

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.setTintCompat
import ch.rmy.android.http_shortcuts.R

class VariableButton : AppCompatImageButton {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setImageResource(R.drawable.ic_variables)
        if (context.isDarkThemeEnabled()) {
            drawable?.setTintCompat(Color.WHITE)
        }
    }
}
