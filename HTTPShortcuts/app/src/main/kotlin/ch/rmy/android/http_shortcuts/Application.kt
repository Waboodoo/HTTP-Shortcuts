package ch.rmy.android.http_shortcuts

import android.content.Context
import androidx.multidex.MultiDex
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.Settings
import com.facebook.stetho.Stetho

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReporting.init(context)
        CrashReporting.enabled = Settings(context).isCrashReportingAllowed

        Stetho.initializeWithDefaults(context)
    }

    public override fun attachBaseContext(base: Context) {
        MultiDex.install(base)
        super.attachBaseContext(base)
    }

    val context: Context
        get() = this

}
