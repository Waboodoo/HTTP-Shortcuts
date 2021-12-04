package ch.rmy.android.http_shortcuts.activities.editor.body

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import io.reactivex.Completable

class RequestBodyViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    val parameters: ListLiveData<Parameter>
        get() = getShortcut(persistedRealm)!!
            .parameters
            .toLiveData()

    fun setRequestBodyType(type: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.requestBodyType = type
                if (type != Shortcut.REQUEST_BODY_TYPE_FORM_DATA) {
                    shortcut.parameters
                        .filter { it.isFileParameter || it.isFilesParameter }
                        .forEach { parameter ->
                            parameter.deleteFromRealm()
                        }
                }
            }
        }

    fun moveParameter(oldPosition: Int, newPosition: Int): Completable =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameters = shortcut.parameters
            parameters.move(oldPosition, newPosition)
        }

    fun addStringParameter(key: String, value: String) =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameters = shortcut.parameters
            parameters.add(
                Parameter(
                    type = Parameter.TYPE_STRING,
                    key = key.trim(),
                    value = value
                )
            )
        }

    fun addFileParameter(key: String, fileName: String, multiple: Boolean) =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameters = shortcut.parameters
            parameters.add(
                Parameter(
                    type = if (multiple) Parameter.TYPE_FILES else Parameter.TYPE_FILE,
                    key = key.trim(),
                    fileName = fileName
                )
            )
        }

    fun updateParameter(parameterId: String, key: String, value: String = "", fileName: String = "") =
        Transactions.commit { realm ->
            val shortcut = getShortcut(realm) ?: return@commit
            val parameter = shortcut.parameters.find { it.id == parameterId } ?: return@commit
            parameter.key = key.trim()
            parameter.value = value
            parameter.fileName = fileName
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
                shortcut.contentType = contentType.trim()
                shortcut.bodyContent = bodyContent
            }
        }
}
