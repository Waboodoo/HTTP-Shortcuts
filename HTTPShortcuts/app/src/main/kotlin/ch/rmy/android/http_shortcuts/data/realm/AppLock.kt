package ch.rmy.android.http_shortcuts.data.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Deprecated("Only used in Realm-to-Room migration")
class AppLock() : RealmObject {
    @PrimaryKey
    var id: Long = 0
    var passwordHash: String = ""
    var useBiometrics: Boolean = false
}
