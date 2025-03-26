package ch.rmy.android.http_shortcuts.data.realm.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Option")
class RealmOption() : RealmObject {
    @PrimaryKey
    var id: String = ""
    var label: String = ""
    var value: String = ""
}
