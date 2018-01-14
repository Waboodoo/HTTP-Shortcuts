package ch.rmy.android.http_shortcuts

import android.content.Context
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.NotificationUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import com.facebook.stetho.Stetho
import com.uphyca.stetho_realm.RealmInspectorModulesProvider

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReporting.init(context)
        CrashReporting.enabled = Settings(context).isCrashReportingAllowed

        Controller.init(context)
        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(context).build())
                        .build()
        )

        NotificationUtil.createChannels(context)
    }

    val context: Context
        get() = this

}
