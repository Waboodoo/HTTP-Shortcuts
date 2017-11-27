package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.Canvas
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView

class ShortcutListDecorator(context: Context, resId: Int) : RecyclerView.ItemDecoration() {

    private val divider = ContextCompat.getDrawable(context, resId)!!

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val paddingLeft = parent.paddingLeft
        val paddingRight = parent.width - parent.paddingRight

        for (i in 0..parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val paddingTop = child.bottom + params.bottomMargin
            val paddingBottom = paddingTop + divider.intrinsicHeight
            divider.setBounds(paddingLeft, paddingTop, paddingRight, paddingBottom)
            divider.draw(canvas)
        }
    }

}