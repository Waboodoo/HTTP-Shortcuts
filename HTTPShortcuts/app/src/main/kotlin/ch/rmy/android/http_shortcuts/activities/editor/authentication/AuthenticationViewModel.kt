package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import io.reactivex.Completable

class AuthenticationViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setAuthenticationMethod(authenticationMethod: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.authentication = authenticationMethod
        }

    fun setCredentials(username: String, password: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.username = username
                shortcut.password = password
            }
        }

}