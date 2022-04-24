package ch.rmy.android.http_shortcuts.data.enums

enum class ShortcutClickBehavior(val type: String) {
    RUN("run"),
    EDIT("edit"),
    MENU("menu");

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            values().firstOrNull { it.type == type }
                ?: RUN
    }
}
