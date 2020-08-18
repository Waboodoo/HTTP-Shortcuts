package ch.rmy.android.http_shortcuts.activities.editor.response

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import io.reactivex.Completable

class ResponseViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setResponseHandling(
        uiType: String,
        successOutput: String,
        failureOutput: String,
        successMessage: String,
        includeMetaInfo: Boolean,
    ): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)
                ?.responseHandling
                ?.let { responseHandling ->
                    responseHandling.uiType = uiType
                    responseHandling.successOutput = successOutput
                    responseHandling.failureOutput = failureOutput
                    responseHandling.successMessage = successMessage
                    responseHandling.includeMetaInfo = includeMetaInfo
                }
        }

}