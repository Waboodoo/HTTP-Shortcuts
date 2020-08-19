package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.extensions.tryOrLog

object IconMigration {

    private const val PREF_NAME = "migrations"
    private const val PREF_ICON_MIGRATION = "icon_migration"

    fun migrateIfNeeded(context: Context) {
        tryOrLog {
            val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            if (!preferences.getBoolean(PREF_ICON_MIGRATION, false)) {
                preferences.edit().putBoolean(PREF_ICON_MIGRATION, true).apply()

                RealmFactory.withRealm { realm ->
                    Repository.getShortcuts(realm)
                        .forEach { shortcut ->
                            IconUtil.getIconURI(context, shortcut.iconName, external = true)
                        }
                }
            }
        }
    }

}