package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.annotations.PersistedName

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "ResponseHandling")
class RealmResponseHandling() : EmbeddedRealmObject {
    var actions: RealmList<String> = realmListOf()
    var uiType: String = ""
    var successOutput: String = ""
    var failureOutput: String = ""
    var contentType: String? = null
    var charset: String? = null
    var successMessage: String = ""
    var includeMetaInfo: Boolean = false
    var jsonArrayAsTable: Boolean = true
    var monospace: Boolean = false
    var fontSize: Int? = null
    var javaScriptEnabled: Boolean = false
    var storeDirectoryId: WorkingDirectoryId? = null
    var storeFileName: String? = null
    var replaceFileIfExists: Boolean = false
}
