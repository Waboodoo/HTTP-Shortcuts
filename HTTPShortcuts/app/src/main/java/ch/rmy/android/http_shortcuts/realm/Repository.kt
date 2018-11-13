package ch.rmy.android.http_shortcuts.realm

import ch.rmy.android.http_shortcuts.realm.models.AppLock
import ch.rmy.android.http_shortcuts.realm.models.Base
import ch.rmy.android.http_shortcuts.realm.models.Category
import ch.rmy.android.http_shortcuts.realm.models.HasId
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Variable
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where

object Repository {

    internal fun getBase(realm: Realm): Base? =
            realm
                    .where<Base>()
                    .findFirst()

    internal fun getShortcuts(realm: Realm): Collection<Shortcut> =
            realm
                    .where<Shortcut>()
                    .notEqualTo(HasId.FIELD_ID, Shortcut.TEMPORARY_ID)
                    .findAll()

    internal fun getCategoryById(realm: Realm, categoryId: Long): Category? =
            realm
                    .where<Category>()
                    .equalTo(HasId.FIELD_ID, categoryId)
                    .findFirst()

    internal fun getShortcutById(realm: Realm, shortcutId: Long): Shortcut? =
            realm
                    .where<Shortcut>()
                    .equalTo(HasId.FIELD_ID, shortcutId)
                    .findFirst()

    internal fun getShortcutByName(realm: Realm, shortcutName: String): Shortcut? =
            realm
                    .where<Shortcut>()
                    .equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE)
                    .findFirst()

    internal fun getVariableById(realm: Realm, variableId: Long): Variable? =
            realm
                    .where<Variable>()
                    .equalTo(HasId.FIELD_ID, variableId)
                    .findFirst()

    internal fun getVariableByKey(realm: Realm, key: String): Variable? =
            realm
                    .where<Variable>()
                    .equalTo(Variable.FIELD_KEY, key)
                    .findFirst()

    internal fun getShortcutsPendingExecution(realm: Realm): RealmResults<PendingExecution> =
            realm
                    .where<PendingExecution>()
                    .sort(PendingExecution.FIELD_ENQUEUED_AT)
                    .findAll()

    internal fun getShortcutPendingExecution(realm: Realm, shortcutId: Long): PendingExecution? =
            realm
                    .where<PendingExecution>()
                    .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
                    .findFirst()

    internal fun getAppLock(realm: Realm): AppLock? =
            realm
                    .where<AppLock>()
                    .findFirst()

}