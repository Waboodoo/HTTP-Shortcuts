package ch.rmy.android.http_shortcuts.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan

class OrderedListSpan(
    private val number: Int,
) : LeadingMarginSpan {

    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        layout: Layout,
    ) {
        if (start == (text as Spanned).getSpanStart(this)) {
            canvas.drawText("$number.", x.toFloat(), baseline.toFloat(), paint)
        }
    }

    override fun getLeadingMargin(first: Boolean): Int = MARGIN

    companion object {
        private const val MARGIN = 60
    }
}
