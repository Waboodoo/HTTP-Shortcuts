package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import io.reactivex.Completable

class ResponseViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setFeedbackType(type: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.feedback = type
        }

}