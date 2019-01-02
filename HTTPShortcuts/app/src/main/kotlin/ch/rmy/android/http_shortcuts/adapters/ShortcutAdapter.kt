package ch.rmy.android.http_shortcuts.adapters

import android.content.Context

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut

abstract class ShortcutAdapter internal constructor(context: Context, shortcuts: ListLiveData<Shortcut>) : BaseAdapter<Shortcut>(context, shortcuts) {

    internal var shortcutsPendingExecution: List<PendingExecution> = emptyList()

    override val emptyMarkerStringResource = R.string.no_shortcuts

    fun setPendingShortcuts(shortcutsPendingExecution: List<PendingExecution>) {
        this.shortcutsPendingExecution = shortcutsPendingExecution
        notifyDataSetChanged()
    }

}
