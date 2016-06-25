package ch.rmy.android.http_shortcuts.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class DatabaseMigration implements RealmMigration {

    public static final int VERSION = 1;

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        for (long version = oldVersion + 1; version <= newVersion; version++) {
            migrate(realm, (int) version);
        }
    }

    private void migrate(DynamicRealm realm, int newVersion) {
        RealmSchema schema = realm.getSchema();
        switch (newVersion) {
            case 1: { // 1.10.0
                schema.get("Shortcut").addField("acceptAllCertificates", boolean.class);
                break;
            }
            default:
                throw new IllegalArgumentException("Missing migration for version " + newVersion);
        }
    }

}
