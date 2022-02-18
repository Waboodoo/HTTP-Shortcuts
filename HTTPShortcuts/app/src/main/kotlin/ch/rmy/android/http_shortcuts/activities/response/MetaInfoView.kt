package ch.rmy.android.http_shortcuts.activities.response

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.FrameLayout
import ch.rmy.android.framework.extensions.layoutInflater
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.http_shortcuts.databinding.ViewMetaInfoBinding

class MetaInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewMetaInfoBinding.inflate(layoutInflater, this, true)

    fun showGeneralInfo(data: List<Pair<String, String>>) {
        binding.generalContainer.visible = true
        binding.generalView.text = toStyledSpan(data)
    }

    fun showHeaders(headers: List<Pair<String, String>>) {
        binding.headersContainer.visible = true
        binding.headersView.text = toStyledSpan(headers)
    }

    companion object {

        private fun toStyledSpan(data: List<Pair<String, String>>): CharSequence {
            val builder = SpannableStringBuilder()
            var offset = 0
            data.forEach { (name, value) ->
                if (offset != 0) {
                    builder.append("\n")
                    offset++
                }
                val line = "$name: $value"
                builder.append(line)
                tryOrIgnore {
                    builder.setSpan(StyleSpan(Typeface.BOLD), offset, offset + name.length + 1, 0)
                }
                offset += line.length
            }
            return builder
        }
    }
}
