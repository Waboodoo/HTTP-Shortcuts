package ch.rmy.android.http_shortcuts.data.models

import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.extensions.toInstant
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class WorkingDirectory : RealmObject {

    @PrimaryKey
    var id: WorkingDirectoryId = ""

    var name: String = ""
    var directory: String = ""

    var directoryUri: Uri
        get() = directory.toUri()
        set(value) {
            directory = value.toString()
        }

    private var accessed: RealmInstant? = null

    val lastAccessed
        get() = accessed?.toInstant()

    fun touch() {
        accessed = RealmInstant.now()
    }

    fun validate() {
        require(id.isUUID()) {
            "Invalid directory ID found, must be UUID: $id"
        }
        require(name.isNotEmpty()) {
            "Invalid directory name for working directory"
        }
        require(directory.startsWith("content:", ignoreCase = true)) {
            "Invalid directory URI for working directory"
        }
    }

    companion object {
        const val FIELD_ID = "id"
        const val FIELD_NAME = "name"
    }
}
