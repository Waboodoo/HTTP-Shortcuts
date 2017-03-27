package ch.rmy.android.http_shortcuts.realm;

import io.realm.Realm;
import io.realm.RealmConfiguration;

class RealmFactory {

    private static RealmConfiguration config;

    static Realm getRealm() {
        return Realm.getInstance(getConfiguration());
    }

    private static RealmConfiguration getConfiguration() {
        if (config == null) {
            config = new RealmConfiguration.Builder()
                    .schemaVersion(DatabaseMigration.VERSION)
                    .migration(new DatabaseMigration())
                    .build();
        }

        return config;
    }

}
