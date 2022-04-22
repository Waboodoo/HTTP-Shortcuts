package ch.rmy.android.framework.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.addRippleAnimation
import ch.rmy.android.framework.extensions.layoutInflater
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.ViewSimpleListItemBinding

class SimpleListItemView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewSimpleListItemBinding.inflate(layoutInflater, this)

    var title: CharSequence = ""
        set(value) {
            field = value
            binding.simpleListItemTitle.text = value
        }

    var subtitle: CharSequence? = null
        set(value) {
            field = value
            binding.simpleListItemSubtitle.text = value
            binding.simpleListItemSubtitle.isVisible = !value.isNullOrEmpty()
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

    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text, R.attr.subtitle)
    }
}
