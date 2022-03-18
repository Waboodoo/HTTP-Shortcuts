package ch.rmy.android.http_shortcuts.data.domains

import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.data.models.AppLockModel
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.CategoryModel
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.data.models.WidgetModel
import io.realm.Case
import io.realm.RealmQuery
import io.realm.kotlin.where

fun RealmContext.getBase(): RealmQuery<BaseModel> =
    realmInstance
        .where<BaseModel>()

fun RealmContext.getCategoryById(categoryId: String): RealmQuery<CategoryModel> =
    realmInstance
        .where<CategoryModel>()
        .equalTo(CategoryModel.FIELD_ID, categoryId)

fun RealmContext.getShortcutById(shortcutId: String): RealmQuery<ShortcutModel> =
    realmInstance
        .where<ShortcutModel>()
        .equalTo(ShortcutModel.FIELD_ID, shortcutId)

fun RealmContext.getTemporaryShortcut(): RealmQuery<ShortcutModel> =
    getShortcutById(ShortcutModel.TEMPORARY_ID)

fun RealmContext.getShortcutByNameOrId(shortcutNameOrId: String): RealmQuery<ShortcutModel> =
    realmInstance
        .where<ShortcutModel>()
        .equalTo(ShortcutModel.FIELD_ID, shortcutNameOrId)
        .or()
        .equalTo(ShortcutModel.FIELD_NAME, shortcutNameOrId, Case.INSENSITIVE)

fun RealmContext.getVariableById(variableId: String): RealmQuery<VariableModel> =
    realmInstance
        .where<VariableModel>()
        .equalTo(VariableModel.FIELD_ID, variableId)

fun RealmContext.getVariableByKeyOrId(keyOrId: String): RealmQuery<VariableModel> =
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

fun RealmContext.getPendingExecutions(shortcutId: String? = null, waitForNetwork: Boolean? = null): RealmQuery<PendingExecutionModel> =
    realmInstance
        .where<PendingExecutionModel>()
        .mapIfNotNull(shortcutId) {
            equalTo(PendingExecutionModel.FIELD_SHORTCUT_ID, it)
        }
        .mapIfNotNull(waitForNetwork) {
            equalTo(PendingExecutionModel.FIELD_WAIT_FOR_NETWORK, it)
        }
        .sort(PendingExecutionModel.FIELD_ENQUEUED_AT)

fun RealmContext.getPendingExecution(id: String): RealmQuery<PendingExecutionModel> =
    realmInstance
        .where<PendingExecutionModel>()
        .equalTo(PendingExecutionModel.FIELD_ID, id)

fun RealmContext.getAppLock(): RealmQuery<AppLockModel> =
    realmInstance
        .where<AppLockModel>()

fun RealmContext.getWidgetsByIds(widgetIds: List<Int>): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .`in`(WidgetModel.FIELD_WIDGET_ID, widgetIds.toTypedArray())

fun RealmContext.getDeadWidgets(): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .isNull(WidgetModel.FIELD_SHORTCUT)

fun RealmContext.getWidgetsForShortcut(shortcutId: String): RealmQuery<WidgetModel> =
    realmInstance
        .where<WidgetModel>()
        .equalTo("${WidgetModel.FIELD_SHORTCUT}.${ShortcutModel.FIELD_ID}", shortcutId)
