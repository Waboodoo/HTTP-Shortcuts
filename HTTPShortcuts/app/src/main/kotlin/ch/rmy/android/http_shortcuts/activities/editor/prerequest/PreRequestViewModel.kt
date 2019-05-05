package ch.rmy.android.http_shortcuts.activities.editor.prerequest

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.extensions.commitAsync
import io.reactivex.Completable

class PreRequestViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setCodeOnPrepare(code: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.codeOnPrepare = code
        }

}