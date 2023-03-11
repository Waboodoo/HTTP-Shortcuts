package ch.rmy.android.http_shortcuts.data.domains

import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import io.realm.Case
import io.realm.RealmQuery
import io.realm.Sort
import io.realm.kotlin.where

fun RealmContext.getBase(): RealmQuery<Base> =
    realmInstance
        .where()

fun RealmContext.getCategoryById(categoryId: CategoryId): RealmQuery<Category> =
    realmInstance
        .where<Category>()
        .equalTo(Category.FIELD_ID, categoryId)

fun RealmContext.getShortcutById(shortcutId: ShortcutId): RealmQuery<Shortcut> =
    realmInstance
        .where<Shortcut>()
        .equalTo(Shortcut.FIELD_ID, shortcutId)

fun RealmContext.getTemporaryShortcut(): RealmQuery<Shortcut> =
    getShortcutById(Shortcut.TEMPORARY_ID)

fun RealmContext.getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): RealmQuery<Shortcut> =
    realmInstance
        .where<Shortcut>()
        .equalTo(Shortcut.FIELD_ID, shortcutNameOrId)
        .or()
        .equalTo(Shortcut.FIELD_NAME, shortcutNameOrId, Case.INSENSITIVE)

fun RealmContext.getVariableById(variableId: VariableId): RealmQuery<Variable> =
    realmInstance
        .where<Variable>()
        .equalTo(Variable.FIELD_ID, variableId)

fun RealmContext.getVariableByKeyOrId(keyOrId: VariableKeyOrId): RealmQuery<Variable> =
    realmInstance
        .where<Variable>()
        .beginGroup()
        .equalTo(Variable.FIELD_KEY, keyOrId)
        .and()
        .notEqualTo(Variable.FIELD_ID, Variable.TEMPORARY_ID)
        .endGroup()
        .or()
        .equalTo(Variable.FIELD_ID, keyOrId)

fun RealmContext.getTemporaryVariable(): RealmQuery<Variable> =
    realmInstance
        .where<Variable>()
        .equalTo(Variable.FIELD_ID, Variable.TEMPORARY_ID)

fun RealmContext.getPendingExecutions(shortcutId: ShortcutId? = null, waitForNetwork: Boolean? = null): RealmQuery<PendingExecution> =
    realmInstance
        .where<PendingExecution>()
        .runIfNotNull(shortcutId) {
            equalTo(PendingExecution.FIELD_SHORTCUT_ID, it)
        }
        .runIfNotNull(waitForNetwork) {
            equalTo(PendingExecution.FIELD_WAIT_FOR_NETWORK, it)
        }
        .sort(PendingExecution.FIELD_ENQUEUED_AT)

fun RealmContext.getPendingExecution(id: ExecutionId): RealmQuery<PendingExecution> =
    realmInstance
        .where<PendingExecution>()
        .equalTo(PendingExecution.FIELD_ID, id)

fun RealmContext.getAppLock(): RealmQuery<AppLock> =
    realmInstance
        .where()

fun RealmContext.getWidgetsByIds(widgetIds: List<Int>): RealmQuery<Widget> =
    realmInstance
        .where<Widget>()
        .`in`(Widget.FIELD_WIDGET_ID, widgetIds.toTypedArray())

fun RealmContext.getDeadWidgets(): RealmQuery<Widget> =
    realmInstance
        .where<Widget>()
        .isNull(Widget.FIELD_SHORTCUT)

fun RealmContext.getWidgetsForShortcut(shortcutId: ShortcutId): RealmQuery<Widget> =
    realmInstance
        .where<Widget>()
        .equalTo("${Widget.FIELD_SHORTCUT}.${Shortcut.FIELD_ID}", shortcutId)

fun RealmContext.getHistoryEvents(): RealmQuery<HistoryEvent> =
    realmInstance
        .where<HistoryEvent>()
        .sort(HistoryEvent.FIELD_TIME, Sort.DESCENDING)
