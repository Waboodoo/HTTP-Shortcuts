package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId
import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryRepository
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.models.AppConfig
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import javax.inject.Inject

class GetUsedWorkingDirectoryIdsUseCase
@Inject
constructor(
    private val workingDirectoryRepository: WorkingDirectoryRepository,
    private val requestParameterRepository: RequestParameterRepository,
) {
    suspend operator fun invoke(shortcuts: List<Shortcut>, appConfig: AppConfig): Set<WorkingDirectoryId> =
        buildSet {
            val workingDirectories = workingDirectoryRepository.getWorkingDirectories()
            val requestParametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts)
            shortcuts.forEach { shortcut ->
                shortcut.responseStoreDirectoryId?.let(::add)
                addAll(extractFromCode(shortcut.codeOnPrepare, workingDirectories))
                addAll(extractFromCode(shortcut.codeOnSuccess, workingDirectories))
                addAll(extractFromCode(shortcut.codeOnFailure, workingDirectories))
                shortcut.fileUploadSourceDirectoryId?.let(::add)
                requestParametersByShortcutId[shortcut.id]?.forEach { parameter ->
                    if (parameter.parameterType == ParameterType.FILE && parameter.fileUploadType == FileUploadType.FILE) {
                        parameter.fileUploadSourceDirectoryId?.let(::add)
                    }
                }
            }
            addAll(extractFromCode(appConfig.globalCode, workingDirectories))
        }

    private fun extractFromCode(code: String, workingDirectories: List<WorkingDirectory>): Sequence<WorkingDirectoryId> =
        GET_DIRECTORY_CALL_SITE_REGEX.findAll(code)
            .mapNotNull { matchResult ->
                workingDirectories
                    .findByNameOrId(nameOrId = matchResult.groupValues[1].drop(1).dropLast(1))
                    ?.id
            }

    private fun List<WorkingDirectory>.findByNameOrId(nameOrId: String): WorkingDirectory? =
        find { it.id == nameOrId || it.name.equals(nameOrId, ignoreCase = true) }

    companion object {
        private val GET_DIRECTORY_CALL_SITE_REGEX =
            """getDirectory\(("[^"]+"|'[^']+')\)""".toRegex()
    }
}
