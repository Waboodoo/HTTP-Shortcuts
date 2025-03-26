package ch.rmy.android.http_shortcuts.data.realm.models

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName

@Deprecated("Only used in Realm-to-Room migration")
@PersistedName(name = "Base")
class RealmBase : RealmObject {
    var title: String? = null
    var globalCode: String? = null
    var categories: RealmList<RealmCategory> = realmListOf()
    var variables: RealmList<RealmVariable> = realmListOf()
}
