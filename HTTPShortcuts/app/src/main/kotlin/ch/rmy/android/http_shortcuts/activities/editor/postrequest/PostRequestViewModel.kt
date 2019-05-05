package ch.rmy.android.http_shortcuts.activities.editor.postrequest

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.extensions.commitAsync
import io.reactivex.Completable

class PostRequestViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setCode(successCode: String, failureCode: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.codeOnSuccess = successCode
                shortcut.codeOnFailure = failureCode
            }
        }

}