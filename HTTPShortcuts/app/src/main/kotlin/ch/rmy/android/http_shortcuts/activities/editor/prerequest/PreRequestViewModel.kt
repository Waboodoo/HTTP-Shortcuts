package ch.rmy.android.http_shortcuts.activities.editor.prerequest

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import io.reactivex.Completable

class PreRequestViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setCodeOnPrepare(code: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.codeOnPrepare = code
        }

}