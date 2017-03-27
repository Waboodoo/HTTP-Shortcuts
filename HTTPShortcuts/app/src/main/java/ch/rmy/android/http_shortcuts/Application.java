package ch.rmy.android.http_shortcuts;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import ch.rmy.android.http_shortcuts.realm.Controller;

public class Application extends android.app.Application {

    public void onCreate() {
        super.onCreate();
        Controller.init(this);
        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                            .build());
        }
    }

}
