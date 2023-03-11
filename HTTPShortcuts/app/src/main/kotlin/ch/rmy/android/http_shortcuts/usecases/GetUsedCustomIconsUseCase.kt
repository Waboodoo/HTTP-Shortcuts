package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetUsedCustomIconsUseCase
@Inject
constructor(
    private val appRepository: AppRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) {

    @CheckResult
    suspend operator fun invoke(
        shortcutIds: Collection<ShortcutId>? = null,
        includeTemporaryShortcut: Boolean = false,
    ): List<ShortcutIcon.CustomIcon> =
        withContext(Dispatchers.Default) {
            val base = appRepository.getBase()
            val temporaryShortcut = if (includeTemporaryShortcut) {
                getTemporaryShortcut()
            } else {
                null
            }
            getCustomShortcutIcons(base, shortcutIds, temporaryShortcut)
        }

    private suspend fun getTemporaryShortcut(): Shortcut? =
        try {
            temporaryShortcutRepository.getTemporaryShortcut()
        } catch (e: NoSuchElementException) {
            null
        }

    private fun getCustomShortcutIcons(base: Base, shortcutIds: Collection<ShortcutId>?, temporaryShortcut: Shortcut?) =
        base.shortcuts
            .runIfNotNull(temporaryShortcut, List<Shortcut>::plus)
            .asSequence()
            .runIfNotNull(shortcutIds) { ids ->
                filter { shortcut -> shortcut.id in ids }
            }
            .map { it.icon }
            .filterIsInstance(ShortcutIcon.CustomIcon::class.java)
            .plus(
                getReferencedIconNames(base, temporaryShortcut)
                    .map { fileName ->
                        ShortcutIcon.CustomIcon(fileName)
                    }
            )
            .distinct()
            .toList()

    private fun getReferencedIconNames(base: Base, temporaryShortcut: Shortcut?): Set<String> =
        IconUtil.extractCustomIconNames(base.globalCode ?: "")
            .plus(
                base.shortcuts
                    .runIfNotNull(temporaryShortcut, List<Shortcut>::plus)
                    .flatMap(::getReferencedIconNames)
            )

    private fun getReferencedIconNames(shortcut: Shortcut): Set<String> =
        IconUtil.extractCustomIconNames(shortcut.codeOnSuccess)
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnFailure))
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnPrepare))
}
