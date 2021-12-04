package ch.rmy.android.http_shortcuts.data

import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import io.realm.Realm
import io.realm.RealmObject
import java.util.Date

object DataSource {

    fun getShortcutByNameOrId(shortcutNameOrId: String): Shortcut? =
        getFromRealm { realm ->
            Repository.getShortcutByNameOrId(realm, shortcutNameOrId)
        }

    fun getPendingExecution(id: String): PendingExecution? =
        getFromRealm { realm ->
            Repository.getPendingExecution(realm, id)
        }

    fun getNextPendingExecution(waitForNetwork: Boolean): PendingExecution? =
        getFromRealm { realm ->
            Repository.getPendingExecutions(realm, waitForNetwork = waitForNetwork)
                .minByOrNull { it.waitUntil ?: Date(0) }
        }

    fun deletePendingExecution(id: String) {
        transaction { realm ->
            Repository.getPendingExecution(realm, id)
                ?.deleteFromRealm()
        }
    }

    private fun <T : RealmObject> getFromRealm(query: (realm: Realm) -> T?): T? =
        RealmFactory.getInstance().createRealm().use { realm ->
            return query(realm)?.detachFromRealm()
        }

    private fun useRealm(action: (realm: Realm) -> Unit) {
        RealmFactory.getInstance().createRealm().use(action)
    }

    private fun transaction(transaction: Realm.Transaction) {
        useRealm { realm ->
            realm.executeTransaction(transaction)
        }
    }
}
