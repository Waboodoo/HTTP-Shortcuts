package ch.rmy.android.http_shortcuts.data.realm.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
class CertificatePin() : RealmObject {
    @PrimaryKey
    var id: String = ""
    var pattern: String = ""
    var hash: String = ""
}
