package ch.rmy.android.http_shortcuts.data.enums

enum class CategoryBackgroundType(val type: String) {
    WHITE("white"),
    BLACK("black"),
    WALLPAPER("wallpaper");

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values().firstOrNull { it.type == type }
                ?: WHITE
    }
}
