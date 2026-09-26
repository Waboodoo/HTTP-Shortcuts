package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.extensions.getRequestHeadersForShortcut
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcut
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import javax.inject.Inject

class SupportsTextSharingUseCase
@Inject
constructor(
    private val shareUtil: ShareUtil,
    private val shortcutRepository: ShortcutRepository,
    private val globalVariableRepository: GlobalVariableRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
) {
    suspend operator fun invoke(shortcutId: ShortcutId): Boolean {
        val shortcut = shortcutRepository.getShortcutById(shortcutId)
        val variables = globalVariableRepository.getGlobalVariables()
        val globalVariableIds = shareUtil.getTextShareGlobalVariables(variables).ids()
        return shareUtil.isTextShareTarget(
            shortcut = shortcut,
            headers = requestHeaderRepository.getRequestHeadersForShortcut(shortcut),
            parameters = requestParameterRepository.getRequestParametersForShortcut(shortcut),
            globalVariableIds = globalVariableIds,
            variableManager = VariableManager(variables),
        )
    }
}
