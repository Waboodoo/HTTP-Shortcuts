package ch.rmy.android.http_shortcuts.data

import android.content.Context
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.migration.DatabaseMigration
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList
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
        private var instance: RealmFactory? = null

        fun init(context: Context) {
            if (instance != null) {
                return
            }

            Realm.init(context)
            instance = RealmFactory(BuildConfig.REALM_ENCRYPTION_KEY.toByteArray())
                .apply {
                    createRealm().use { realm ->
                        if (Repository.getBase(realm) == null) {
                            setupBase(context, realm)
                        }
                    }
                }
        }

        fun getInstance(): RealmFactory = instance!!

        private fun setupBase(context: Context, realm: Realm) {
            val defaultCategoryName = context.getString(R.string.shortcuts)
            realm.executeTransaction {
                val defaultCategory = Category.createNew(defaultCategoryName)
                defaultCategory.id = newUUID()

                val newBase = Base().apply {
                    categories = RealmList()
                    variables = RealmList()
                    categories.add(defaultCategory)
                    version = DatabaseMigration.VERSION
                }
                it.copyToRealm(newBase)
            }
        }

    }

}
