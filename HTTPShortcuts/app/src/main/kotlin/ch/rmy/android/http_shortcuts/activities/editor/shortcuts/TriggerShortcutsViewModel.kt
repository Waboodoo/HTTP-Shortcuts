package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import android.app.Application
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager
import io.reactivex.Completable

class TriggerShortcutsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    val triggerShortcuts: ListLiveData<ShortcutPlaceholder> = object : ListLiveData<ShortcutPlaceholder>() {

        private val shortcuts = Repository.getShortcuts(persistedRealm)

        private val observer = object : Observer<Shortcut?> {
            override fun onChanged(t: Shortcut?) {
                onChange()
            }
        }

        override fun onActive() {
            shortcut.observeForever(observer)
        }

        override fun onInactive() {
            shortcut.removeObserver(observer)
        }

        override fun getValue(): List<ShortcutPlaceholder>? =
            getTriggeredShortcuts()
                .mapNotNull {
                    if (it.shortcutId.isEmpty()) {
                        shortcut.value
                    } else {
                        shortcuts.firstOrNull { shortcut ->
                            shortcut.id == it.shortcutId
                        }
                    }
                        ?.let { ShortcutPlaceholder.fromShortcut(it) }
                }

    }

    private fun getTriggeredShortcuts(): List<TriggerShortcutManager.TriggeredShortcut> =
        shortcut.value
            ?.codeOnPrepare
            ?.let {
                TriggerShortcutManager.getTriggeredShortcutsFromCode(it)
            }
            ?: emptyList()

    fun changeShortcutPosition(oldPosition: Int, newPosition: Int): Completable =
        mutateShortcutList { shortcuts ->
            shortcuts.add(newPosition, shortcuts.removeAt(oldPosition))
        }

    fun addShortcut(shortcutId: String): Completable =
        mutateShortcutList { shortcuts ->
            shortcuts.add(TriggerShortcutManager.TriggeredShortcut(shortcutId))
        }

    fun removeShortcut(shortcutId: String): Completable =
        mutateShortcutList { shortcuts ->
            shortcuts.removeIf { it.shortcutId == shortcutId }
        }

    private fun mutateShortcutList(action: (MutableList<TriggerShortcutManager.TriggeredShortcut>) -> Unit): Completable {
        val shortcuts = getTriggeredShortcuts().toMutableList()
        return Transactions.commit { realm ->
            action.invoke(shortcuts)
            getShortcut(realm)?.codeOnPrepare =
                TriggerShortcutManager.getCodeFromTriggeredShortcuts(shortcuts)
        }
    }

}