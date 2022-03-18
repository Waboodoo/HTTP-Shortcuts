package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(name = "AppLock")
open class AppLockModel(
    var passwordHash: String = "",
) : RealmObject() {
    @PrimaryKey
    var id: Long = 0
}
