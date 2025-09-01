package ch.rmy.android.http_shortcuts.icons

import android.graphics.Color

object Icons {
    const val DEFAULT_TINT_PREFIX = "black_"

    enum class TintColor(val prefix: String, val color: Int) {
        BLACK("black_", Color.BLACK),
        GREY("grey_", Color.GRAY),
        WHITE("white_", Color.WHITE),
    }

    val PREFIXES = setOf("black_", "grey_", "white_", "freepik_", "bitsies_", "flat_grey_", "flat_color_")
}
