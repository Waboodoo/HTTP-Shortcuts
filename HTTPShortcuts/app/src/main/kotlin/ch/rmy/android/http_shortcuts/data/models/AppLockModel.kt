package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(name = "AppLock")
open class AppLockModel(
    var passwordHash: String = "",
) : RealmModel {
    @PrimaryKey
    var id: Long = 0
}
