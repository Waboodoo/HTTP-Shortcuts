package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class GridLayoutManager(context: Context) : GridLayoutManager(context, getNumberOfColumns(context)) {

    private var empty: Boolean = false

    init {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (position == 0 && empty) spanCount else 1
        }
    }

    fun setEmpty(empty: Boolean) {
        this.empty = empty
    }

    companion object {

        fun getNumberOfColumns(context: Context): Int {
            val displayMetrics = context.resources.displayMetrics
            val dpWidth = displayMetrics.widthPixels / displayMetrics.density
            return (dpWidth / 90).toInt()
        }

    }

}
