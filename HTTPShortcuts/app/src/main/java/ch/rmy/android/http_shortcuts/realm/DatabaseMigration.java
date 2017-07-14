package ch.rmy.android.http_shortcuts.realm;

import android.text.TextUtils;

import ch.rmy.android.http_shortcuts.utils.UUIDUtils;
import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmResults;
import io.realm.RealmSchema;

public class DatabaseMigration implements RealmMigration {

    public static final int VERSION = 9;

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
                    base.setList("variables", new RealmList<DynamicRealmObject>());
                }

                break;
            }
            case 3: { // 1.12.0
                schema.get("Variable").addField("rememberValue", boolean.class);
                break;
            }
            case 4: { // 1.13.0
                schema.get("Variable").addField("flags", int.class);
                break;
            }
            case 5: { // 1.16.0
                schema.get("Category").addField("layoutType", String.class);
                RealmResults<DynamicRealmObject> categories = realm.where("Category").findAll();
                for (DynamicRealmObject category : categories) {
                    category.setString("layoutType", "linear_list");
                }
                break;
            }
            case 6: { // 1.16.0
                schema.get("Shortcut").addField("authentication", String.class);
                RealmResults<DynamicRealmObject> shortcuts = realm.where("Shortcut").findAll();
                for (DynamicRealmObject shortcut : shortcuts) {
                    if (!TextUtils.isEmpty(shortcut.getString("username")) || !TextUtils.isEmpty(shortcut.getString("password"))) {
                        shortcut.setString("authentication", "basic");
                    }
                }
                break;
            }
            case 7: { // 1.16.0
                schema.get("Base").addField("version", long.class);
                break;
            }
            case 8: { // 1.16.1
                schema.get("Shortcut").addField("launcherShortcut", boolean.class);
                break;
            }
            case 9: { // 1.16.2
                schema.get("Parameter").addField("id", String.class);
                schema.get("Header").addField("id", String.class);
                schema.get("Option").addField("id", String.class);
                RealmResults<DynamicRealmObject> parameters = realm.where("Parameter").findAll();
                for (DynamicRealmObject parameter : parameters) {
                    parameter.setString("id", UUIDUtils.INSTANCE.create());
                }
                RealmResults<DynamicRealmObject> headers = realm.where("Header").findAll();
                for (DynamicRealmObject header : headers) {
                    header.setString("id", UUIDUtils.INSTANCE.create());
                }
                RealmResults<DynamicRealmObject> options = realm.where("Option").findAll();
                for (DynamicRealmObject option : options) {
                    option.setString("id", UUIDUtils.INSTANCE.create());
                }
                schema.get("Parameter").addPrimaryKey("id");
                schema.get("Header").addPrimaryKey("id");
                schema.get("Option").addPrimaryKey("id");
                break;
            }

            default:
                throw new IllegalArgumentException("Missing migration for version " + newVersion);
        }

        updateVersionNumber(realm, newVersion);
    }

    private void updateVersionNumber(DynamicRealm realm, long version) {
        DynamicRealmObject base = realm.where("Base").findFirst();
        if (base != null && version >= 7) {
            base.setLong("version", version);
        }
    }

}
