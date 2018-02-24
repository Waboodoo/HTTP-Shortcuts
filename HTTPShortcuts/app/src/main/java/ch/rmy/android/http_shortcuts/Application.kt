package ch.rmy.android.http_shortcuts

import android.content.Context
import android.support.multidex.MultiDex
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.NotificationUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import com.facebook.stetho.Stetho

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReporting.init(context)
        CrashReporting.enabled = Settings(context).isCrashReportingAllowed

        Controller.init(context)
        Stetho.initializeWithDefaults(context)

        NotificationUtil.createChannels(context)
    }

    public override fun attachBaseContext(base: Context) {
        MultiDex.install(base)
        super.attachBaseContext(base)
    }

    val context: Context
        get() = this

}
