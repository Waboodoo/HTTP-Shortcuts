package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.extensions.commitAsync
import ch.rmy.android.http_shortcuts.extensions.toLiveData

class RequestHeadersViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    val headers: ListLiveData<Header>
        get() = getShortcut(persistedRealm)!!
            .headers
            .toLiveData()

    fun moveHeader(oldPosition: Int, newPosition: Int) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcut(realm) ?: return@commitAsync
            val headers = shortcut.headers
            headers.move(oldPosition, newPosition)
        }

    fun addHeader(key: String, value: String) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcut(realm) ?: return@commitAsync
            val headers = shortcut.headers
            headers.add(Header(
                key = key,
                value = value
            ))
        }

    fun updateHeader(headerId: String, key: String, value: String) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcut(realm) ?: return@commitAsync
            val header = shortcut.headers.find { it.id == headerId } ?: return@commitAsync
            header.key = key
            header.value = value
        }

    fun removeHeader(headerId: String) =
        persistedRealm.commitAsync { realm ->
            val shortcut = getShortcut(realm) ?: return@commitAsync
            val header = shortcut.headers.find { it.id == headerId } ?: return@commitAsync
            header.deleteFromRealm()
        }

}