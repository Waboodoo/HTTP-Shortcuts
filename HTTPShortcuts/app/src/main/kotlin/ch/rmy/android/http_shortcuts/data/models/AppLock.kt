package ch.rmy.android.http_shortcuts.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class AppLock() : RealmObject {
    @PrimaryKey
    var id: Long = 0

    var passwordHash: String = ""
    var useBiometrics: Boolean = false

    constructor(passwordHash: String, useBiometrics: Boolean) : this() {
        this.passwordHash = passwordHash
        this.useBiometrics = useBiometrics
    }
}
