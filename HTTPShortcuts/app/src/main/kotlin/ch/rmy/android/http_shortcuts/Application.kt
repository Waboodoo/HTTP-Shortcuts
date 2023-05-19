package ch.rmy.android.http_shortcuts

import android.content.Context
import ch.rmy.android.framework.extensions.GlobalLogger
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponentProvider
import ch.rmy.android.http_shortcuts.dagger.DaggerApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.Settings
import org.conscrypt.Conscrypt
import java.security.Security
import javax.inject.Inject

class Application : android.app.Application(), ApplicationComponentProvider {

    private val context: Context
        get() = this

    override val applicationComponent: ApplicationComponent by lazy(LazyThreadSafetyMode.NONE) {
        DaggerApplicationComponent.builder()
            .application(this)
            .build()
    }

    @Inject
    lateinit var localeHelper: LocaleHelper

    override fun onCreate() {
        super.onCreate()
        applicationComponent.inject(this)
        localeHelper.applyLocaleFromSettings()

        Security.insertProviderAt(Conscrypt.newProvider(), 1)

        Logging.initCrashReporting(context)
        GlobalLogger.registerLogging(Logging)

        RealmFactoryImpl.init(applicationContext)

        DarkThemeHelper.applyDarkThemeSettings(Settings(context).darkThemeSetting)
    }
}
