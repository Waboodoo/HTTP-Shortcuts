package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Parameter(
    @PrimaryKey
    @Required
    override var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = "",
    var type: String = TYPE_STRING,
    var fileName: String = ""
) : RealmObject(), HasId {

    fun isSameAs(other: Parameter) =
        other.key == key
            && other.value == value
            && other.type == type
            && other.fileName == fileName

    val isFileParameter: Boolean
        get() = type == TYPE_FILE

    val isFilesParameter: Boolean
        get() = type == TYPE_FILES

    fun validate() {
        if (type !in setOf(TYPE_STRING, TYPE_FILE, TYPE_FILES)) {
            throw IllegalArgumentException("Invalid parameter type: $type")
        }
    }

    companion object {

        const val TYPE_STRING = "string"
        const val TYPE_FILE = "file"
        const val TYPE_FILES = "files"

    }

}
