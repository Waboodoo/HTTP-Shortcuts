package ch.rmy.android.http_shortcuts.data.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class AppLock() : RealmObject {
    @PrimaryKey
    var id: Long = 0

    var passwordHash: String = ""

    constructor(passwordHash: String) : this() {
        this.passwordHash = passwordHash
    }
}
