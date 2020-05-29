package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import io.reactivex.Completable

class ScriptingViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    val shortcuts: ListLiveData<Shortcut>
        get() = object : ListLiveData<Shortcut>() {

            private val base: Base = Repository.getBase(persistedRealm)!!

            override fun getValue(): List<Shortcut>? =
                base.categories.flatMap {
                    it.shortcuts
                }
        }

    fun setCode(prepareCode: String, successCode: String, failureCode: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.codeOnPrepare = prepareCode.trim()
                shortcut.codeOnSuccess = successCode.trim()
                shortcut.codeOnFailure = failureCode.trim()
            }
        }

}