package ch.rmy.android.http_shortcuts.realm

import io.realm.Realm
import io.realm.RealmConfiguration

internal object RealmFactory {

    val realm: Realm
        get() = Realm.getInstance(configuration)

    private val configuration: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .schemaVersion(DatabaseMigration.VERSION)
                .migration(DatabaseMigration())
                .build()
    }

}
