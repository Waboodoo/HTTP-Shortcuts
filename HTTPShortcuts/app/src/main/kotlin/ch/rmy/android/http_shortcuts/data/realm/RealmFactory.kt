package ch.rmy.android.http_shortcuts.data.realm

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.data.realm.migration.RealmDatabaseMigration
import ch.rmy.android.http_shortcuts.data.realm.models.RealmAppLock
import ch.rmy.android.http_shortcuts.data.realm.models.RealmBase
import ch.rmy.android.http_shortcuts.data.realm.models.RealmCategory
import ch.rmy.android.http_shortcuts.data.realm.models.RealmCertificatePin
import ch.rmy.android.http_shortcuts.data.realm.models.RealmFileUploadOptions
import ch.rmy.android.http_shortcuts.data.realm.models.RealmHeader
import ch.rmy.android.http_shortcuts.data.realm.models.RealmOption
import ch.rmy.android.http_shortcuts.data.realm.models.RealmParameter
import ch.rmy.android.http_shortcuts.data.realm.models.RealmRepetition
import ch.rmy.android.http_shortcuts.data.realm.models.RealmResponseHandling
import ch.rmy.android.http_shortcuts.data.realm.models.RealmSection
import ch.rmy.android.http_shortcuts.data.realm.models.RealmShortcut
import ch.rmy.android.http_shortcuts.data.realm.models.RealmVariable
import ch.rmy.android.http_shortcuts.data.realm.models.RealmWidget
import ch.rmy.android.http_shortcuts.data.realm.models.RealmWorkingDirectory
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import java.io.File

object RealmFactory {
    private const val DB_NAME = "shortcuts_db_v2"

    fun createRealm(): Realm? {
        logInfo("initializing Realm")
        val configuration = createConfiguration()

        val realmFile = File(configuration.path)
        if (!realmFile.exists()) {
            logInfo("No Realm file found, assuming fresh installation")
            return null
        }

        logInfo("Creating RealmFactoryImpl instance")
        return Realm.open(configuration)
    }

    private fun createConfiguration(): RealmConfiguration =
        RealmConfiguration.Builder(
            setOf(
                RealmAppLock::class,
                RealmBase::class,
                RealmCategory::class,
                RealmCertificatePin::class,
                RealmFileUploadOptions::class,
                RealmHeader::class,
                RealmOption::class,
                RealmParameter::class,
                RealmRepetition::class,
                RealmResponseHandling::class,
                RealmSection::class,
                RealmShortcut::class,
                RealmVariable::class,
                RealmWidget::class,
                RealmWorkingDirectory::class,
            ),
        )
            .schemaVersion(90L)
            .migration(RealmDatabaseMigration())
            .name(DB_NAME)
            .build()
}
