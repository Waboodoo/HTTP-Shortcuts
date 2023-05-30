package ch.rmy.android.http_shortcuts.data.models

import io.realm.kotlin.types.EmbeddedRealmObject

class FileUploadOptions() : EmbeddedRealmObject {

    constructor(
        useImageEditor: Boolean,
    ) : this() {
        this.useImageEditor = useImageEditor
    }

    var useImageEditor: Boolean = false
        private set

    fun isSameAs(other: FileUploadOptions?): Boolean =
        other?.useImageEditor == useImageEditor
}
