package ch.rmy.android.http_shortcuts.data

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm

object DataSource {

    fun getShortcutByNameOrId(shortcutNameOrId: String): Shortcut? {
        RealmFactory.getInstance().createRealm().use { realm ->
            return Repository.getShortcutByNameOrId(realm, shortcutNameOrId)?.detachFromRealm()
        }
    }

}