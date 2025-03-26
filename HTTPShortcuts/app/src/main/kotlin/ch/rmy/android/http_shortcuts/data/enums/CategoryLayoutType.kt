package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class CategoryLayoutType(val type: String) {
    LINEAR_LIST("linear_list"),
    DENSE_GRID("dense_grid"),
    MEDIUM_GRID("medium_grid"),
    WIDE_GRID("wide_grid"),
    ;

    override fun toString() =
        type

    companion object {
        fun parse(type: String): CategoryLayoutType? =
            entries.find { it.type == type }
    }
}
