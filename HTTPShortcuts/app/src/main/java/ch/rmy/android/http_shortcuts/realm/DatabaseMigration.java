package ch.rmy.android.http_shortcuts.realm;

import ch.rmy.android.http_shortcuts.realm.models.Base;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class DatabaseMigration implements RealmMigration {

    public static final int VERSION = 2;

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
            case 2: { // 1.11.0
                RealmObjectSchema resolvedVariableSchema = schema.create("ResolvedVariable")
                        .addField("key", String.class)
                        .addField("value", String.class);
                RealmObjectSchema optionSchema = schema.create("Option")
                        .addField("label", String.class)
                        .addField("value", String.class);
                RealmObjectSchema variableSchema = schema.create("Variable")
                        .addField("id", long.class).addPrimaryKey("id")
                        .addField("key", String.class).setRequired("key", true)
                        .addField("type", String.class).setRequired("type", true)
                        .addField("value", String.class)
                        .addField("title", String.class).setRequired("title", true)
                        .addField("urlEncode", boolean.class)
                        .addField("jsonEncode", boolean.class)
                        .addRealmListField("options", optionSchema);
                schema.get("Base")
                        .addRealmListField("variables", variableSchema);
                schema.get("PendingExecution")
                        .addRealmListField("resolvedVariables", resolvedVariableSchema);

                DynamicRealmObject base = realm.where("Base").findFirst();
                if (base != null) {
                    base.setList("variables", new RealmList<Base>());
                }

                break;
            }

            default:
                throw new IllegalArgumentException("Missing migration for version " + newVersion);
        }
    }

}
