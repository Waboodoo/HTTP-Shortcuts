package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import io.reactivex.Completable

class BasicRequestSettingsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setMethod(method: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.method = method
        }

    fun setUrl(url: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.url = url
        }

}