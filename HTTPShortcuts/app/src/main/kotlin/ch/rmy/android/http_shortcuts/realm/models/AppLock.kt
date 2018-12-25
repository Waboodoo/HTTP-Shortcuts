package ch.rmy.android.http_shortcuts.realm.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class AppLock : RealmObject() {

    @PrimaryKey
    var id: Long = 0

    var passwordHash: String = ""

}