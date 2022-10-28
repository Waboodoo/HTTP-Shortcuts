package ch.rmy.android.framework.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import ch.rmy.android.framework.R
import ch.rmy.android.framework.databinding.ViewPanelButtonBinding
import ch.rmy.android.framework.extensions.addRippleAnimation
import ch.rmy.android.framework.extensions.layoutInflater

class PanelButton
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPanelButtonBinding.inflate(layoutInflater, this)

    var title: CharSequence = ""
        set(value) {
            field = value
            binding.panelButtonTitle.text = value
        }

    var subtitle: CharSequence = ""
        set(value) {
            field = value
            binding.panelButtonSubtitle.text = value
        }

    init {
        addRippleAnimation()
        if (attrs != null) {
            context.obtainStyledAttributes(attrs, ATTRIBUTE_IDS).use { a ->
                title = a.getText(ATTRIBUTE_IDS.indexOf(android.R.attr.text)) ?: ""
                subtitle = a.getText(ATTRIBUTE_IDS.indexOf(R.attr.subtitle)) ?: ""
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        val alpha = if (enabled) 1f else 0.4f
        binding.panelButtonTitle.alpha = alpha
        binding.panelButtonSubtitle.alpha = alpha
    }

    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text, R.attr.subtitle)
    }
}
