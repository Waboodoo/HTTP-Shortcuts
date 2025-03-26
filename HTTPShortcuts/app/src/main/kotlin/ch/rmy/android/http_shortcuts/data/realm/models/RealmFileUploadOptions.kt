package ch.rmy.android.http_shortcuts.data.realm.models

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.annotations.PersistedName

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "FileUploadOptions")
class RealmFileUploadOptions : EmbeddedRealmObject {
    var fileUploadType: String = ""
    var file: String? = null
    var useImageEditor: Boolean = false
}
