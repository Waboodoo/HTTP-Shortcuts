package ch.rmy.android.http_shortcuts.data

import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.utils.Destroyable
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import java.io.Closeable

class Controller : Destroyable, Closeable {

    private val realm: Realm = RealmFactory.getInstance().createRealm()

    override fun destroy() {
        if (!realm.isClosed) {
            realm.close()
        }
    }

    override fun close() = destroy()

    fun getBase(): Base =
        Repository.getBase(realm)!!

    fun getVariables(): RealmList<Variable> = Repository.getBase(realm)!!.variables

    fun getShortcutById(id: String) = Repository.getShortcutById(realm, id)

    fun getPendingExecutions(shortcutId: String? = null): RealmResults<PendingExecution> =
        Repository.getPendingExecutions(realm, shortcutId)

    fun getPendingExecution(id: String) =
        Repository.getPendingExecution(realm, id)

    fun getWidgetsByIds(widgetIds: Array<Int>) =
        Repository.getWidgetsByIds(realm, widgetIds)

    fun getWidgetsForShortcut(shortcutId: String) =
        Repository.getWidgetsForShortcut(realm, shortcutId)

}
