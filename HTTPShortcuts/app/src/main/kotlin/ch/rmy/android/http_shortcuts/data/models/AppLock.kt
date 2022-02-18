package ch.rmy.android.http_shortcuts.data.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AppLock(
    var passwordHash: String = "",
) : RealmObject() {
    @PrimaryKey
    var id: Long = 0
}
