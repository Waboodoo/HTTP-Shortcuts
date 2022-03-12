package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import io.reactivex.Single

class GetUsedCustomIconsUseCase(
    private val appRepository: AppRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) {

    @CheckResult
    operator fun invoke(includeTemporaryShortcut: Boolean = false): Single<List<ShortcutIcon.CustomIcon>> =
        appRepository.getBase()
            .flatMap { base ->
                if (includeTemporaryShortcut) {
                    getTemporaryShortcut()
                } else {
                    Single.just(Optional.empty())
                }
                    .map { temporaryShortcutOptional ->
                        getCustomShortcutIcons(base, temporaryShortcutOptional.value)
                    }
            }

    private fun getTemporaryShortcut(): Single<Optional<Shortcut>> =
        temporaryShortcutRepository.getTemporaryShortcut()
            .map { Optional(it) }
            .onErrorReturn { Optional.empty() }

    private fun getCustomShortcutIcons(base: Base, temporaryShortcut: Shortcut?) =
        base.shortcuts
            .mapIfNotNull(temporaryShortcut, List<Shortcut>::plus)
            .asSequence()
            .map { it.icon }
            .filterIsInstance(ShortcutIcon.CustomIcon::class.java)
            .plus(
                getReferencedIconNames(base)
                    .map { fileName ->
                        ShortcutIcon.CustomIcon(fileName)
                    }
            )
            .distinct()
            .toList()

    private fun getReferencedIconNames(base: Base): Set<String> =
        IconUtil.extractCustomIconNames(base.globalCode ?: "")
            .plus(base.shortcuts.flatMap(::getReferencedIconNames))

    private fun getReferencedIconNames(shortcut: Shortcut): Set<String> =
        IconUtil.extractCustomIconNames(shortcut.codeOnSuccess)
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnFailure))
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnPrepare))
}
