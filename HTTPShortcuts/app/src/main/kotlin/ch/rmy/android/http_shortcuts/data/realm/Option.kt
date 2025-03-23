package ch.rmy.android.http_shortcuts.data.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
class Option() : RealmObject {
    @PrimaryKey
    var id: String = ""
    var label: String = ""
    var value: String = ""
}
