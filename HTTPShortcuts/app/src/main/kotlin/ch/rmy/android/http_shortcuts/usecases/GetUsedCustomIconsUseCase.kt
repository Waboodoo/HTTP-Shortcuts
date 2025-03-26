package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetUsedCustomIconsUseCase
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val appConfigRepository: AppConfigRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) {

    @CheckResult
    suspend operator fun invoke(
        shortcutIds: Collection<ShortcutId>? = null,
        includeTemporaryShortcut: Boolean = false,
    ): List<ShortcutIcon.CustomIcon> =
        withContext(Dispatchers.Default) {
            getCustomShortcutIcons(
                shortcuts = shortcutRepository.getShortcuts(),
                globalCode = appConfigRepository.getGlobalCode(),
                shortcutIds = shortcutIds,
                temporaryShortcut = if (includeTemporaryShortcut) {
                    getTemporaryShortcut()
                } else {
                    null
                },
            )
        }

    private suspend fun getTemporaryShortcut(): Shortcut? =
        try {
            temporaryShortcutRepository.getTemporaryShortcut()
        } catch (_: NoSuchElementException) {
            null
        }

    private fun getCustomShortcutIcons(
        shortcuts: List<Shortcut>,
        globalCode: String,
        shortcutIds: Collection<ShortcutId>?,
        temporaryShortcut: Shortcut?,
    ) =
        shortcuts
            .runIfNotNull(temporaryShortcut, List<Shortcut>::plus)
            .asSequence()
            .runIfNotNull(shortcutIds) { ids ->
                filter { shortcut -> shortcut.id in ids }
            }
            .map { it.icon }
            .filterIsInstance(ShortcutIcon.CustomIcon::class.java)
            .plus(
                getReferencedIconNames(shortcuts, globalCode, temporaryShortcut)
                    .map { fileName ->
                        ShortcutIcon.CustomIcon(fileName)
                    },
            )
            .distinct()
            .toList()

    private fun getReferencedIconNames(shortcuts: List<Shortcut>, globalCode: String, temporaryShortcut: Shortcut?): Set<String> =
        IconUtil.extractCustomIconNames(globalCode)
            .plus(
                shortcuts
                    .runIfNotNull(temporaryShortcut, List<Shortcut>::plus)
                    .flatMap(::getReferencedIconNames),
            )

    private fun getReferencedIconNames(shortcut: Shortcut): Set<String> =
        IconUtil.extractCustomIconNames(shortcut.codeOnSuccess)
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnFailure))
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnPrepare))
}
