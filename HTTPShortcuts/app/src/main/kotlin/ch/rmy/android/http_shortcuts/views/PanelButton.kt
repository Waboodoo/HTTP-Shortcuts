package ch.rmy.android.http_shortcuts.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.addRippleAnimation
import ch.rmy.android.http_shortcuts.extensions.setContentView
import kotterknife.bindView

class PanelButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val titleView: TextView by bindView(R.id.panel_button_title)
    private val subtitleView: TextView by bindView(R.id.panel_button_subtitle)

    var title: CharSequence = ""
        set(value) {
            field = value
            titleView.text = value
        }

    var subtitle: CharSequence = ""
        set(value) {
            field = value
            subtitleView.text = value
        }

    init {
        setContentView(R.layout.view_panel_button)
        addRippleAnimation()

        if (attrs != null) {
            var a: TypedArray? = null
            try {
                @SuppressLint("Recycle")
                a = context.obtainStyledAttributes(attrs, ATTRIBUTE_IDS)
                title = a.getText(ATTRIBUTE_IDS.indexOf(android.R.attr.text)) ?: ""
                subtitle = a.getText(ATTRIBUTE_IDS.indexOf(R.attr.subtitle)) ?: ""
            } finally {
                a?.recycle()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        val alpha = if (enabled) 1f else 0.4f
        titleView.alpha = alpha
        subtitleView.alpha = alpha
    }


    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text, R.attr.subtitle)

    }

}