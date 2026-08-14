package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.rmy.android.framework.extensions.GlobalLogger
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import java.security.Security
import javax.inject.Inject
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
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var deviceLocalPreferences: DeviceLocalPreferences

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

        if (!RealmMigrator.check(context)) {
            val errorCount = deviceLocalPreferences.realmErrorCount
            if (errorCount < 10) {
                deviceLocalPreferences.realmErrorCount = errorCount + 1
                logException(RuntimeException("Unmigrated Realm found"))
            }
            unmigratedRealmFound = true
        }

        DarkThemeHelper.applyDarkThemeSettings(userPreferences.darkThemeSetting)
    }

    companion object {
        var unmigratedRealmFound = false
    }
}
