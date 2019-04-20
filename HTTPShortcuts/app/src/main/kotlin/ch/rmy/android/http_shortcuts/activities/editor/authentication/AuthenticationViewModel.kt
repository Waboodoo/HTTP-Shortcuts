package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.extensions.commitAsync
import io.reactivex.Completable

class AuthenticationViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setAuthenticationMethod(authenticationMethod: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.authentication = authenticationMethod
        }

    fun setCredentials(username: String, password: String): Completable =
        persistedRealm.commitAsync { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.username = username
                shortcut.password = password
            }
        }

}