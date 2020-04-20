package ch.rmy.android.http_shortcuts.activities.response

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.extensions.visible
import kotterknife.bindView

class MetaInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val generalInfoContainer: View by bindView(R.id.general_container)
    private val generalView: TextView by bindView(R.id.general_view)
    private val headersContainer: View by bindView(R.id.headers_container)
    private val headersView: TextView by bindView(R.id.headers_view)

    init {
        View.inflate(context, R.layout.view_meta_info, this)
    }

    fun showGeneralInfo(data: List<Pair<String, String>>) {
        generalInfoContainer.visible = true
        generalView.text = toStyledSpan(data)
    }

    fun showHeaders(headers: List<Pair<String, String>>) {
        headersContainer.visible = true
        headersView.text = toStyledSpan(headers)
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
                tryOrLog {
                    builder.setSpan(TypefaceSpan(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)), offset, offset + name.length + 1, 0)
                }
                offset += line.length
            }
            return builder
        }

    }

}