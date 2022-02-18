package ch.rmy.android.framework.utils.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

abstract class ColoredSpan(private val color: Int) : ReplacementSpan() {

    private val typeface by lazy {
        Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    protected open val displayedText: String? = null

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        paint.color = color
        paint.typeface = typeface
        displayedText
            ?.also {
                canvas.drawText(it, 0, it.length, x, y.toFloat(), paint)
            }
            ?: canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        fm?.ascent = paint.fontMetricsInt.ascent
        fm?.bottom = paint.fontMetricsInt.bottom
        fm?.descent = paint.fontMetricsInt.descent
        fm?.leading = paint.fontMetricsInt.leading
        fm?.top = paint.fontMetricsInt.top
        return calculateTextWidth(paint, displayedText ?: text.subSequence(start, end))
    }

    private fun calculateTextWidth(paint: Paint, text: CharSequence): Int {
        paint.color = color
        paint.typeface = typeface
        val textWidth = paint.measureText(text, 0, text.length)
        return textWidth.roundToInt()
    }
}
