package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import io.realm.kotlin.types.EmbeddedRealmObject

class FileUploadOptions : EmbeddedRealmObject {

    var type: FileUploadType
        get() = FileUploadType.parse(fileUploadType)
        set(value) {
            fileUploadType = value.type
        }

    private var fileUploadType: String = FileUploadType.FILE.type

    var file: String? = null

    var useImageEditor: Boolean = false

    fun isSameAs(other: FileUploadOptions): Boolean =
        other.type == type &&
            other.useImageEditor == useImageEditor &&
            other.file == file
}
