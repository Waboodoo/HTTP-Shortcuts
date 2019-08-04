package ch.rmy.android.http_shortcuts.data

import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.HasId
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
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

    internal fun getCategoryById(realm: Realm, categoryId: String): Category? =
        realm
            .where<Category>()
            .equalTo(HasId.FIELD_ID, categoryId)
            .findFirst()

    internal fun getCategoryByIdAsync(realm: Realm, categoryId: String): Category =
        realm
            .where<Category>()
            .equalTo(HasId.FIELD_ID, categoryId)
            .findFirstAsync()

    internal fun getShortcutById(realm: Realm, shortcutId: String): Shortcut? =
        realm
            .where<Shortcut>()
            .equalTo(HasId.FIELD_ID, shortcutId)
            .findFirst()

    internal fun getShortcutByName(realm: Realm, shortcutName: String): Shortcut? =
        realm
            .where<Shortcut>()
            .equalTo(Shortcut.FIELD_NAME, shortcutName, Case.INSENSITIVE)
            .findFirst()

    fun getShortcutByNameOrId(realm: Realm, shortcutNameOrId: String): Shortcut? =
        realm
            .where<Shortcut>()
            .equalTo(HasId.FIELD_ID, shortcutNameOrId)
            .or()
            .equalTo(Shortcut.FIELD_NAME, shortcutNameOrId, Case.INSENSITIVE)
            .findFirst()

    internal fun getVariableById(realm: Realm, variableId: String): Variable? =
        realm
            .where<Variable>()
            .equalTo(HasId.FIELD_ID, variableId)
            .findFirst()

    internal fun getVariableByKey(realm: Realm, key: String): Variable? =
        realm
            .where<Variable>()
            .equalTo(Variable.FIELD_KEY, key)
            .findFirst()

    internal fun getVariableByKeyOrId(realm: Realm, keyOrId: String): Variable? =
        realm
            .where<Variable>()
            .equalTo(Variable.FIELD_KEY, keyOrId)
            .or()
            .equalTo(HasId.FIELD_ID, keyOrId)
            .findFirst()

    internal fun getShortcutsPendingExecution(realm: Realm): RealmResults<PendingExecution> =
        realm
            .where<PendingExecution>()
            .sort(PendingExecution.FIELD_ENQUEUED_AT)
            .findAll()

    internal fun getShortcutPendingExecution(realm: Realm, shortcutId: String): PendingExecution? =
        realm
            .where<PendingExecution>()
            .equalTo(PendingExecution.FIELD_SHORTCUT_ID, shortcutId)
            .findFirst()

    internal fun getAppLock(realm: Realm): AppLock? =
        realm
            .where<AppLock>()
            .findFirst()

    internal fun deleteShortcut(realm: Realm, shortcutId: String) {
        getShortcutById(realm, shortcutId)?.apply {
            headers.deleteAllFromRealm()
            parameters.deleteAllFromRealm()
            deleteFromRealm()
        }
    }

    internal fun copyShortcut(realm: Realm, sourceShortcut: Shortcut, targetShortcutId: String): Shortcut =
        sourceShortcut.detachFromRealm()
            .apply {
                id = targetShortcutId
                parameters.forEach { parameter ->
                    parameter.id = newUUID()
                }
                headers.forEach { header ->
                    header.id = newUUID()
                }
            }
            .let {
                realm.copyToRealmOrUpdate(it)
            }

    internal fun moveShortcut(realm: Realm, shortcutId: String, targetPosition: Int? = null, targetCategoryId: String? = null) {
        val shortcut = getShortcutById(realm, shortcutId) ?: return
        val categories = getBase(realm)?.categories ?: return
        val targetCategory = if (targetCategoryId != null) {
            getCategoryById(realm, targetCategoryId)
        } else {
            categories.first { category -> category.shortcuts.any { it.id == shortcutId } }
        } ?: return

        for (category in categories) {
            category.shortcuts.remove(shortcut)
        }
        if (targetPosition != null) {
            targetCategory.shortcuts.add(targetPosition, shortcut)
        } else {
            targetCategory.shortcuts.add(shortcut)
        }
    }

}