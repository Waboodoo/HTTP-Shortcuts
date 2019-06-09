package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.reactivex.Completable

class RequestBodyViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    val parameters: ListLiveData<Parameter>
        get() = getShortcut(persistedRealm)!!
            .parameters
            .toLiveData()

    fun setRequestBodyType(type: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.requestBodyType = type
        }

    fun moveParameter(oldPosition: Int, newPosition: Int): Completable =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameters = shortcut.parameters
            parameters.move(oldPosition, newPosition)
        }

    fun addParameter(key: String, value: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameters = shortcut.parameters
            parameters.add(Parameter(
                key = key,
                value = value
            ))
        }

    fun updateParameter(parameterId: String, key: String, value: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameter = shortcut.parameters.find { it.id == parameterId } ?: return@commit
            parameter.key = key
            parameter.value = value
        }

    fun removeParameter(parameterId: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameter = shortcut.parameters.find { it.id == parameterId } ?: return@commit
            parameter.deleteFromRealm()
        }

    fun setRequestBody(contentType: String, bodyContent: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.contentType = contentType
                shortcut.bodyContent = bodyContent
            }
        }

}