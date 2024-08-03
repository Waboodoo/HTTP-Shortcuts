package ch.rmy.android.http_shortcuts.data.domains

import ch.rmy.android.framework.data.RealmContext
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.models.AppLock
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.HistoryEvent
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.Widget
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmInstant
import kotlin.time.Duration

fun RealmContext.getBase(): RealmQuery<Base> =
    get()

fun RealmContext.getCategoryById(categoryId: CategoryId): RealmQuery<Category> =
    get("${Category.FIELD_ID} == $0", categoryId)

fun RealmContext.getShortcutById(shortcutId: ShortcutId): RealmQuery<Shortcut> =
    get("${Shortcut.FIELD_ID} == $0", shortcutId)

fun RealmContext.getTemporaryShortcut(): RealmQuery<Shortcut> =
    getShortcutById(Shortcut.TEMPORARY_ID)

fun RealmContext.getShortcutByNameOrId(shortcutNameOrId: ShortcutNameOrId): RealmQuery<Shortcut> =
    get("${Shortcut.FIELD_ID} == $0 OR ${Shortcut.FIELD_NAME} ==[c] $1", shortcutNameOrId, shortcutNameOrId)

fun RealmContext.getVariableById(variableId: VariableId): RealmQuery<Variable> =
    get("${Variable.FIELD_ID} == $0", variableId)

fun RealmContext.getVariableByKeyOrId(keyOrId: VariableKeyOrId): RealmQuery<Variable> =
    get("(${Variable.FIELD_KEY} == $0 AND ${Variable.FIELD_ID} != $1) OR ${Variable.FIELD_ID} == $0", keyOrId, Variable.TEMPORARY_ID)

fun RealmContext.getTemporaryVariable(): RealmQuery<Variable> =
    get("${Variable.FIELD_ID} == $0", Variable.TEMPORARY_ID)

fun RealmContext.getWorkingDirectory(workingDirectoryId: WorkingDirectoryId): RealmQuery<WorkingDirectory> =
    get("${WorkingDirectory.FIELD_ID} == $0", workingDirectoryId)

fun RealmContext.getWorkingDirectoryByNameOrId(workingDirectoryNameOrId: String): RealmQuery<WorkingDirectory> =
    get("${WorkingDirectory.FIELD_ID} == $0 OR ${WorkingDirectory.FIELD_NAME} ==[c] $1", workingDirectoryNameOrId, workingDirectoryNameOrId)

fun RealmContext.getPendingExecutions(shortcutId: ShortcutId? = null, waitForNetwork: Boolean? = null): RealmQuery<PendingExecution> {
    logInfo("getPendingExecution for shortcutId=$shortcutId, waitForNetwork=$waitForNetwork")
    return if (shortcutId != null && waitForNetwork != null) {
        get("${PendingExecution.FIELD_SHORTCUT_ID} == $0 AND ${PendingExecution.FIELD_WAIT_FOR_NETWORK} == $1", shortcutId, waitForNetwork)
    } else if (shortcutId != null) {
        get("${PendingExecution.FIELD_SHORTCUT_ID} == $0", shortcutId)
    } else if (waitForNetwork != null) {
        get("${PendingExecution.FIELD_WAIT_FOR_NETWORK} == $0", waitForNetwork)
    } else {
        get<PendingExecution>()
    }
        .sort(PendingExecution.FIELD_ENQUEUED_AT)
}

fun RealmContext.getPendingExecution(id: ExecutionId): RealmQuery<PendingExecution> =
    get("${PendingExecution.FIELD_ID} == $0", id)

fun RealmContext.getAppLock(): RealmQuery<AppLock> =
    get()

fun RealmContext.getWidgetsByIds(widgetIds: List<Int>): RealmQuery<Widget> =
    get("${Widget.FIELD_WIDGET_ID} IN {${widgetIds.joinToString()}}") // TODO: Figure out how to pass widgetIds as a parameter

fun RealmContext.getDeadWidgets(): RealmQuery<Widget> =
    get("${Widget.FIELD_SHORTCUT} == nil")

fun RealmContext.getWidgetsForShortcut(shortcutId: ShortcutId): RealmQuery<Widget> =
    get("${Widget.FIELD_SHORTCUT}.${Shortcut.FIELD_ID} == $0", shortcutId)

fun RealmContext.getHistoryEvents(): RealmQuery<HistoryEvent> =
    get<HistoryEvent>()
        .sort(HistoryEvent.FIELD_TIME, Sort.DESCENDING)

fun RealmContext.getHistoryEventsOlderThan(age: Duration): RealmQuery<HistoryEvent> =
    get<HistoryEvent>("${HistoryEvent.FIELD_TIME} < $0", RealmInstant.from(RealmInstant.now().epochSeconds - age.inWholeSeconds, 0))
        .sort(HistoryEvent.FIELD_TIME, Sort.DESCENDING)

fun RealmContext.getHistoryEventsNewerThan(age: Duration): RealmQuery<HistoryEvent> =
    get<HistoryEvent>("${HistoryEvent.FIELD_TIME} > $0", RealmInstant.from(RealmInstant.now().epochSeconds - age.inWholeSeconds, 0))
        .sort(HistoryEvent.FIELD_TIME, Sort.DESCENDING)

fun RealmContext.getCertificatePinById(pinId: String): RealmQuery<CertificatePin> =
    get("${CertificatePin.FIELD_ID} == $0", pinId)
