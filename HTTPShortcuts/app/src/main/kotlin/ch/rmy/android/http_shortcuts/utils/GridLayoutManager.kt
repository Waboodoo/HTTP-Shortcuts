package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import ch.rmy.android.framework.extensions.dimen

class GridLayoutManager(
    private val context: Context,
    @DimenRes private val itemWidth: Int,
) : GridLayoutManager(context, getNumberOfColumns(context, itemWidth)) {

    private var empty: Boolean = false

    init {
        spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                if (position == 0 && empty) spanCount else 1
        }
    }

    fun setEmpty(empty: Boolean) {
        this.empty = empty
    }

    fun setTotalWidth(width: Int) {
        spanCount = getNumberOfColumns(context, itemWidth, width.takeUnless { it == 0 })
    }

    companion object {

        fun getNumberOfColumns(context: Context, @DimenRes itemWidth: Int, totalWidth: Int? = null): Int =
            (totalWidth ?: context.resources.displayMetrics.widthPixels) / dimen(context, itemWidth)
    }
}
