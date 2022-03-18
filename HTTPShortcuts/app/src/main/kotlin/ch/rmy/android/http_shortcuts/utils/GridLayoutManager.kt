package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.GridLayoutManager
import ch.rmy.android.framework.extensions.dimen

class GridLayoutManager(context: Context, @DimenRes itemWidth: Int) : GridLayoutManager(context, getNumberOfColumns(context, itemWidth)) {

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

        fun getNumberOfColumns(context: Context, @DimenRes itemWidth: Int): Int {
            val displayMetrics = context.resources.displayMetrics
            return displayMetrics.widthPixels / dimen(context, itemWidth)
        }
    }
}
