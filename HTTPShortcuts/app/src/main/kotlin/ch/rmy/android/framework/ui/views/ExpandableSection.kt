package ch.rmy.android.framework.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import ch.rmy.android.framework.extensions.addRippleAnimation
import ch.rmy.android.framework.extensions.layoutInflater
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.databinding.ViewExpandableSectionBinding
import kotlin.time.Duration.Companion.milliseconds

class ExpandableSection
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewExpandableSectionBinding.inflate(layoutInflater, this)

    var title: CharSequence = ""
        set(value) {
            field = value
            binding.expandableSectionTitle.text = value
        }

    @DrawableRes
    var icon: Int = 0
        set(value) {
            field = value
            binding.expandableSectionIcon.setImageResource(value)
        }

    var expanded: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                binding.expandableSectionChevron
                    .apply {
                        clearAnimation()
                    }
                    .animate()
                    .rotation(if (value) 90f else 0f)
                    .setDuration(CHEVRON_ROTATION_DURATION.inWholeMilliseconds)
                    .start()
            }
        }

    init {
        addRippleAnimation()

        if (attrs != null) {
            var a: TypedArray? = null
            try {
                @SuppressLint("Recycle")
                a = context.obtainStyledAttributes(attrs, ATTRIBUTE_IDS)
                title = a.getText(ATTRIBUTE_IDS.indexOf(android.R.attr.text)) ?: ""
            } finally {
                a?.recycle()
            }
        }
    }

    companion object {

        private val ATTRIBUTE_IDS = intArrayOf(android.R.attr.text)

        private val CHEVRON_ROTATION_DURATION = 200.milliseconds
    }
}
