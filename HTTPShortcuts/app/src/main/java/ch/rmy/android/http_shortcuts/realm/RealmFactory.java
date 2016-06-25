package ch.rmy.android.http_shortcuts.realm;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmFactory {

    private static RealmConfiguration config;

    protected static Realm getRealm(Context context) {
        return Realm.getInstance(getConfiguration(context));
    }

    private static RealmConfiguration getConfiguration(Context context) {
        if (config == null) {
            config = new RealmConfiguration.Builder(context)
                    .schemaVersion(DatabaseMigration.VERSION)
                    .migration(new DatabaseMigration())
                    .build();
        }

        return config;
    }

}
