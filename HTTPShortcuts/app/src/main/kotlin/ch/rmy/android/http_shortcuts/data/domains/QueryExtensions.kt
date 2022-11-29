package ch.rmy.android.http_shortcuts.data.domains

import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.models.AppLockModel
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.HistoryEventModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import io.realm.Case
import io.realm.RealmQuery
import io.realm.Sort
import io.realm.kotlin.where

fun RealmContext.getBase(): RealmQuery<BaseModel> =
    realmInstance
        .where()

fun RealmContext.getCategoryById(categoryId: CategoryId): RealmQuery<CategoryModel> =
    realmInstance
        .where<CategoryModel>()
        .equalTo(CategoryModel.FIELD_ID, categoryId)

fun RealmContext.getShortcutById(shortcutId: ShortcutId): RealmQuery<ShortcutModel> =
    realmInstance
        .where<ShortcutModel>()
        .equalTo(ShortcutModel.FIELD_ID, shortcutId)

fun RealmContext.getTemporaryShortcut(): RealmQuery<ShortcutModel> =
    getShortcutById(ShortcutModel.TEMPORARY_ID)

fun RealmContext.getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): RealmQuery<ShortcutModel> =
    realmInstance
        .where<ShortcutModel>()
        .equalTo(ShortcutModel.FIELD_ID, shortcutNameOrId)
        .or()
        .equalTo(ShortcutModel.FIELD_NAME, shortcutNameOrId, Case.INSENSITIVE)

fun RealmContext.getVariableById(variableId: VariableId): RealmQuery<VariableModel> =
    realmInstance
        .where<VariableModel>()
        .equalTo(VariableModel.FIELD_ID, variableId)

fun RealmContext.getVariableByKeyOrId(keyOrId: VariableKeyOrId): RealmQuery<VariableModel> =
    realmInstance
        .where<VariableModel>()
        .beginGroup()
        .equalTo(VariableModel.FIELD_KEY, keyOrId)
        .and()
        .notEqualTo(VariableModel.FIELD_ID, VariableModel.TEMPORARY_ID)
        .endGroup()
        .or()
        .equalTo(VariableModel.FIELD_ID, keyOrId)

fun RealmContext.getTemporaryVariable(): RealmQuery<VariableModel> =
    realmInstance
        .where<VariableModel>()
        .equalTo(VariableModel.FIELD_ID, VariableModel.TEMPORARY_ID)

fun RealmContext.getPendingExecutions(shortcutId: ShortcutId? = null, waitForNetwork: Boolean? = null): RealmQuery<PendingExecutionModel> =
    realmInstance
        .where<PendingExecutionModel>()
        .runIfNotNull(shortcutId) {
            equalTo(PendingExecutionModel.FIELD_SHORTCUT_ID, it)
        }
        .runIfNotNull(waitForNetwork) {
            equalTo(PendingExecutionModel.FIELD_WAIT_FOR_NETWORK, it)
        }
        .sort(PendingExecutionModel.FIELD_ENQUEUED_AT)

fun RealmContext.getPendingExecution(id: ExecutionId): RealmQuery<PendingExecutionModel> =
    realmInstance
        .where<PendingExecutionModel>()
        .equalTo(PendingExecutionModel.FIELD_ID, id)

fun RealmContext.getAppLock(): RealmQuery<AppLockModel> =
    realmInstance
        .where()

fun RealmContext.getWidgetsByIds(widgetIds: List<Int>): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .`in`(WidgetModel.FIELD_WIDGET_ID, widgetIds.toTypedArray())

fun RealmContext.getDeadWidgets(): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .isNull(WidgetModel.FIELD_SHORTCUT)

fun RealmContext.getWidgetsForShortcut(shortcutId: ShortcutId): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .equalTo("${WidgetModel.FIELD_SHORTCUT}.${ShortcutModel.FIELD_ID}", shortcutId)

fun RealmContext.getHistoryEvents(): RealmQuery<HistoryEventModel> =
    realmInstance
        .where<HistoryEventModel>()
        .sort(HistoryEventModel.FIELD_TIME, Sort.DESCENDING)
