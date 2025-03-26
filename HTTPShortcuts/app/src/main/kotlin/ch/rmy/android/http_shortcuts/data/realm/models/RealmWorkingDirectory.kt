package ch.rmy.android.http_shortcuts.data.realm.models

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "RealmWorkingDirectory")
class RealmWorkingDirectory : RealmObject {
    @PrimaryKey
    var id: WorkingDirectoryId = ""
    var name: String = ""
    var directory: String = ""
    var accessed: RealmInstant? = null
}
