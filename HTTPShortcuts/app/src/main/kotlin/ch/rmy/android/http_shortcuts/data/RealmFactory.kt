package ch.rmy.android.http_shortcuts.data

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.migration.DatabaseMigration
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.extensions.logInfo
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import com.getkeepsafe.relinker.MissingLibraryException
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmList

internal class RealmFactory {

    fun createRealm(): Realm = Realm.getInstance(configuration)

    class RealmNotFoundException(e: Throwable) : Exception(e)

    companion object {

        private const val DB_NAME = "shortcuts_db_v2"
        private var instance: RealmFactory? = null

        fun init(context: Context) {
            if (instance != null) {
                return
            }

            try {
                Realm.init(context)
                instance = RealmFactory()
                    .apply {
                        createRealm().use { realm ->
                            if (Repository.getBase(realm) == null) {
                                setupBase(context, realm)
                            }
                        }
                    }
            } catch (e: MissingLibraryException) {
                logInfo("Realm binary not found")
                throw RealmNotFoundException(e)
            }
        }

        fun getInstance(): RealmFactory = instance!!

        fun <T> withRealm(block: (realm: Realm) -> T): T =
            getInstance().createRealm().use(block)

        private fun setupBase(context: Context, realm: Realm) {
            val defaultCategoryName = context.getString(R.string.shortcuts)
            realm.executeTransaction {
                val defaultCategory = Category(defaultCategoryName)
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

        private val configuration: RealmConfiguration by lazy {
            RealmConfiguration.Builder()
                .schemaVersion(DatabaseMigration.VERSION)
                .migration(DatabaseMigration())
                .name(DB_NAME)
                .build()
        }

    }

}
