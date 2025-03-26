package ch.rmy.android.http_shortcuts.extensions

import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut

suspend fun RequestHeaderRepository.getRequestHeadersForShortcut(shortcut: Shortcut): List<RequestHeader> =
    if (shortcut.executionType == ShortcutExecutionType.HTTP) {
        getRequestHeadersByShortcutId(shortcut.id)
    } else {
        emptyList()
    }

suspend fun RequestHeaderRepository.getRequestHeadersForShortcuts(shortcuts: List<Shortcut>): Map<ShortcutId, List<RequestHeader>> =
    getRequestHeadersByShortcutIds(shortcuts.filter { it.executionType == ShortcutExecutionType.HTTP }.ids())

suspend fun RequestParameterRepository.getRequestParametersForShortcut(shortcut: Shortcut): List<RequestParameter> =
    if (shortcut.usesRequestParameters()) {
        getRequestParametersByShortcutId(shortcut.id)
    } else {
        emptyList()
    }

suspend fun RequestParameterRepository.getRequestParametersForShortcuts(shortcuts: List<Shortcut>): Map<ShortcutId, List<RequestParameter>> =
    getRequestParametersByShortcutIds(shortcuts.filter { it.usesRequestParameters() }.ids())
