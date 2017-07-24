package ch.rmy.android.http_shortcuts.adapters

import android.content.Context

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import io.realm.RealmChangeListener
import io.realm.RealmResults

abstract class ShortcutAdapter internal constructor(context: Context) : BaseAdapter<Shortcut>(context) {

    private val changeListener = RealmChangeListener<RealmResults<PendingExecution>> { notifyDataSetChanged() }
    internal var shortcutsPendingExecution: RealmResults<PendingExecution>? = null

    override val emptyMarkerStringResource = R.string.no_shortcuts

    fun setPendingShortcuts(shortcutsPendingExecution: RealmResults<PendingExecution>) {
        this.shortcutsPendingExecution?.removeChangeListener(changeListener)
        this.shortcutsPendingExecution = shortcutsPendingExecution
        this.shortcutsPendingExecution?.addChangeListener(changeListener)
    }

}
