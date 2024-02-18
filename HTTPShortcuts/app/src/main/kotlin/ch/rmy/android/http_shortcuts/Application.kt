package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import ch.rmy.android.framework.extensions.GlobalLogger
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.HiltAndroidApp
import org.conscrypt.Conscrypt
import java.security.Security
import javax.inject.Inject

@HiltAndroidApp
class Application : android.app.Application(), Configuration.Provider {

    init {
        instance = this
    }

    private val context: Context
        get() = this

    @Inject
    lateinit var localeHelper: LocaleHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

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

        RealmFactoryImpl.init(applicationContext)

        DarkThemeHelper.applyDarkThemeSettings(Settings(context).darkThemeSetting)
    }

    companion object {
        lateinit var instance: Application
    }
}
