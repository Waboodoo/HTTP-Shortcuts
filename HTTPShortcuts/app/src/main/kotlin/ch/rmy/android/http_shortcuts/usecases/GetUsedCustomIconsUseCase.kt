package ch.rmy.android.http_shortcuts.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.BaseModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.utils.IconUtil
import io.reactivex.Single
import javax.inject.Inject

class GetUsedCustomIconsUseCase
@Inject
constructor(
    private val appRepository: AppRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
) {

    @CheckResult
    operator fun invoke(
        shortcutIds: Collection<ShortcutId>? = null,
        includeTemporaryShortcut: Boolean = false,
    ): Single<List<ShortcutIcon.CustomIcon>> =
        appRepository.getBase()
            .flatMap { base ->
                if (includeTemporaryShortcut) {
                    getTemporaryShortcut()
                } else {
                    Single.just(Optional.empty())
                }
                    .map { temporaryShortcutOptional ->
                        getCustomShortcutIcons(base, shortcutIds, temporaryShortcutOptional.value)
                    }
            }

    private fun getTemporaryShortcut(): Single<Optional<ShortcutModel>> =
        temporaryShortcutRepository.getTemporaryShortcut()
            .map { Optional(it) }
            .onErrorReturn { Optional.empty() }

    private fun getCustomShortcutIcons(base: BaseModel, shortcutIds: Collection<ShortcutId>?, temporaryShortcut: ShortcutModel?) =
        base.shortcuts
            .runIfNotNull(temporaryShortcut, List<ShortcutModel>::plus)
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

    private fun getReferencedIconNames(base: BaseModel, temporaryShortcut: ShortcutModel?): Set<String> =
        IconUtil.extractCustomIconNames(base.globalCode ?: "")
            .plus(
                base.shortcuts
                    .runIfNotNull(temporaryShortcut, List<ShortcutModel>::plus)
                    .flatMap(::getReferencedIconNames)
            )

    private fun getReferencedIconNames(shortcut: ShortcutModel): Set<String> =
        IconUtil.extractCustomIconNames(shortcut.codeOnSuccess)
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnFailure))
            .plus(IconUtil.extractCustomIconNames(shortcut.codeOnPrepare))
}
