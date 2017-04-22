package ch.rmy.android.http_shortcuts;

import android.text.TextUtils;

import com.bugsnag.android.Bugsnag;
import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import ch.rmy.android.http_shortcuts.realm.Controller;

public class Application extends android.app.Application {

    public void onCreate() {
        super.onCreate();
        if (!TextUtils.isEmpty(BuildConfig.BUGSNAG_API_KEY)) {
            Bugsnag.init(this, BuildConfig.BUGSNAG_API_KEY);
        }
        Controller.init(this);
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
                        .build()
        );
    }

}
