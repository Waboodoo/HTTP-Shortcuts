package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class CategoryLayoutType(
    val type: String,
    val supportsHorizontalDragging: Boolean = false,
    val legacyAlias: String? = null,
) {
    LINEAR_LIST("linear_list"),
    DENSE_GRID("dense_grid", supportsHorizontalDragging = true, legacyAlias = "grid"),
    MEDIUM_GRID("medium_grid", supportsHorizontalDragging = true),
    WIDE_GRID("wide_grid", supportsHorizontalDragging = true);

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values().firstOrNull { it.type == type || (it.legacyAlias != null && it.legacyAlias == type) }
                ?: LINEAR_LIST
    }
}
