package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import io.realm.annotations.Required

@RealmClass(name = "Parameter")
open class ParameterModel(
    @PrimaryKey
    @Required
    var id: String = newUUID(),
    @Required
    var key: String = "",
    @Required
    var value: String = "",
    var type: String = TYPE_STRING,
    var fileName: String = "",
) : RealmObject() {

    fun isSameAs(other: ParameterModel) =
        other.key == key &&
            other.value == value &&
            other.type == type &&
            other.fileName == fileName

    val isStringParameter: Boolean
        get() = type == TYPE_STRING

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
