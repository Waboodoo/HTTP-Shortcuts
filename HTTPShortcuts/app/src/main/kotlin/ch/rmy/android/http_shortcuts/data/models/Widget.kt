package ch.rmy.android.http_shortcuts.data.models

data class Widget(
    val widgetId: Int,
    val shortcut: Shortcut? = null,
    val labelColor: String? = null,
    val showLabel: Boolean = true,
    val showIcon: Boolean = true,
    val iconScale: Float = 1f,
)
