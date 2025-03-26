package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.rmy.android.framework.extensions.GlobalLogger
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.realm.RealmToRoomMigration
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.HiltAndroidApp
import java.security.Security
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.conscrypt.Conscrypt

@HiltAndroidApp
class Application : android.app.Application(), Configuration.Provider {
    private val context: Context
        get() = this

    @Inject
    lateinit var localeHelper: LocaleHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var realmToRoomMigration: RealmToRoomMigration

    override val workManagerConfiguration: Configuration by lazy {
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        localeHelper.applyLocaleFromSettings()

        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        Logging.initCrashReporting(context)
        GlobalLogger.registerLogging(Logging)

        if (realmToRoomMigration.needsMigration()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    realmToRoomMigration.migrate()
                } catch (e: Exception) {
                    logException(e)
                    startupError = e.message ?: e.toString()
                }
            }
        }

        DarkThemeHelper.applyDarkThemeSettings(Settings(context).darkThemeSetting)
    }

    companion object {
        var startupError: String? = null
    }
}
