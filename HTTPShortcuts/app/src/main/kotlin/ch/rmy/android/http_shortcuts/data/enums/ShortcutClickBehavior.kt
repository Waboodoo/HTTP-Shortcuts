package ch.rmy.android.http_shortcuts.data.enums

import androidx.compose.runtime.Stable

@Stable
enum class ShortcutClickBehavior(val type: String) {
    RUN("run"),
    EDIT("edit"),
    MENU("menu");

    override fun toString() =
        type

    companion object {
        fun parse(type: String?) =
            entries.firstOrNull { it.type == type }
                ?: RUN
    }
}
