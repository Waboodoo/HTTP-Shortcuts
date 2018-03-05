package ch.rmy.android.http_shortcuts.realm

import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.File

internal class RealmFactory(private val encryptionKey: ByteArray) {

    fun createRealm(): Realm {
        val realmFile = File(configuration.realmDirectory, configuration.realmFileName)
        val legacyRealmFile = File(unencryptedLegacyConfiguration.realmDirectory, unencryptedLegacyConfiguration.realmFileName)

        if (legacyRealmFile.exists() && !realmFile.exists()) {
            Realm.getInstance(unencryptedLegacyConfiguration).use {
                it.writeEncryptedCopyTo(realmFile, encryptionKey)
            }
        }
        return Realm.getInstance(configuration)
    }

    private val configuration: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .schemaVersion(DatabaseMigration.VERSION)
                .migration(DatabaseMigration())
                .encryptionKey(encryptionKey)
                .name(DB_NAME)
                .build()
    }

    private val unencryptedLegacyConfiguration: RealmConfiguration by lazy {
        RealmConfiguration.Builder()
                .schemaVersion(DatabaseMigration.VERSION)
                .migration(DatabaseMigration())
                .build()
    }

    companion object {

        private const val DB_NAME = "shortcuts_db"

    }

}
