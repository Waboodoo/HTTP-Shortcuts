package ch.rmy.android.http_shortcuts.import_export;


import android.text.TextUtils;

import ch.rmy.android.http_shortcuts.realm.DatabaseMigration;
import ch.rmy.android.http_shortcuts.realm.models.Base;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;

class ImportMigrator {

    static void migrate(Base base) {
        for (long version = base.getVersion() + 1; version <= DatabaseMigration.VERSION; version++) {
            migrate(base, (int) version);
        }
    }

    private static void migrate(Base base, int newVersion) {
        switch (newVersion) {
            case 5: { // 1.16.0
                for (Category category : base.getCategories()) {
                    category.setLayoutType("linear_list");
                }
                break;
            }
            case 6: { // 1.16.0
                for (Category category : base.getCategories()) {
                    for (Shortcut shortcut : category.getShortcuts()) {
                        if (!TextUtils.isEmpty(shortcut.getUsername()) || !TextUtils.isEmpty(shortcut.getPassword())) {
                            shortcut.setAuthentication("basic");
                        }
                    }
                }
                break;
            }
        }
    }

}
