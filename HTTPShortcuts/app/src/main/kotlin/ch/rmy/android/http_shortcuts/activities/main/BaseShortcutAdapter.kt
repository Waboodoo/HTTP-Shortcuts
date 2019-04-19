package ch.rmy.android.http_shortcuts.activities.main

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseAdapter
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.color

abstract class BaseShortcutAdapter internal constructor(context: Context, shortcuts: ListLiveData<Shortcut>) : BaseAdapter<Shortcut>(context, shortcuts) {

    var textColor: TextColor = TextColor.DARK
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    internal var shortcutsPendingExecution: List<PendingExecution> = emptyList()

    override val emptyMarker = EmptyMarker(
        context.getString(R.string.empty_state_shortcuts),
        context.getString(R.string.empty_state_shortcuts_instructions)
    )

    fun setPendingShortcuts(shortcutsPendingExecution: List<PendingExecution>) {
        this.shortcutsPendingExecution = shortcutsPendingExecution
        notifyDataSetChanged()
    }

    protected val nameTextColor
        get() = when (textColor) {
            TextColor.BRIGHT -> color(context, R.color.text_color_primary_bright)
            TextColor.DARK -> color(context, R.color.text_color_primary_dark)
        }

    protected val descriptionTextColor
        get() = when (textColor) {
            TextColor.BRIGHT -> color(context, R.color.text_color_secondary_bright)
            TextColor.DARK -> color(context, R.color.text_color_secondary_dark)
        }

    enum class TextColor {
        BRIGHT, DARK
    }

}
