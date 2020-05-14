package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.multidex.MultiDex
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.IconMigration
import ch.rmy.android.http_shortcuts.utils.Settings
import com.facebook.stetho.Stetho
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException

class Application : android.app.Application() {

    var isRealmAvailable: Boolean = false
        private set

    override fun onCreate() {
        super.onCreate()

        val settings = Settings(context)

        CrashReporting.init(context)
        CrashReporting.enabled = settings.isCrashReportingAllowed

        Stetho.initializeWithDefaults(context)

        RxJavaPlugins.setErrorHandler { error ->
            if (error.cause !is IOException) {
                logException(error)
            }
        }

        try {
            RealmFactory.init(applicationContext)
            isRealmAvailable = true
        } catch (e: RealmFactory.RealmNotFoundException) {
            // Nothing to do here...
        }

        IconMigration.migrateIfNeeded(context)

        DarkThemeHelper.applyDarkThemeSettings(settings.darkThemeSetting)
    }

    public override fun attachBaseContext(base: Context) {
        MultiDex.install(base)
        super.attachBaseContext(base)
    }

    val context: Context
        get() = this

}
