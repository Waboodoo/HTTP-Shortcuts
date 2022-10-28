package ch.rmy.android.framework.data

import io.realm.Realm

interface RealmFactory {

    fun createRealm(): Realm
}
