package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Repository.getBase
import ch.rmy.android.http_shortcuts.data.Repository.getBaseAsync
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.reactivex.Completable

class GlobalScriptingViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    var hasChanges: MutableLiveData<Boolean> = MutableLiveData(false)

    val base: LiveData<Base?> by lazy {
        getBaseAsync(persistedRealm)
            .toLiveData()
    }

    var iconPickerShortcutPlaceholder: String? = null

    fun setCode(globalCode: String): Completable =
        Transactions.commit { realm ->
            getBase(realm)?.let { base ->
                base.globalCode = globalCode.trim().takeUnlessEmpty()
            }
        }
}
