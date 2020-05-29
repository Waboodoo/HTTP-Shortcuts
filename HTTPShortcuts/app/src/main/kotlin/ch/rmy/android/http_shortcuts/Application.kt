package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.multidex.MultiDex
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.logging.Logging
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

        Logging.initCrashReporting(context)

        Stetho.initializeWithDefaults(context)

        RxJavaPlugins.setErrorHandler { error ->
            if (error.cause !is IOException) {
                logException(error)
            }
        }

        try {
            RealmFactory.init(applicationContext)
            isRealmAvailable = true
            IconMigration.migrateIfNeeded(context)
        } catch (e: RealmFactory.RealmNotFoundException) {
            // Nothing to do here...
        }

        DarkThemeHelper.applyDarkThemeSettings(Settings(context).darkThemeSetting)
    }

    public override fun attachBaseContext(base: Context) {
        MultiDex.install(base)
        super.attachBaseContext(base)
    }

    val context: Context
        get() = this

}
