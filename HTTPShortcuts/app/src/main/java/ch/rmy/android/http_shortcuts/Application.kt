package ch.rmy.android.http_shortcuts

import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.Settings
import com.facebook.stetho.Stetho
import com.uphyca.stetho_realm.RealmInspectorModulesProvider

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()

        CrashReporting.init(this)
        CrashReporting.enabled = Settings(this).isCrashReportingAllowed

        Controller.init(this)
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build()
        )
    }

}
