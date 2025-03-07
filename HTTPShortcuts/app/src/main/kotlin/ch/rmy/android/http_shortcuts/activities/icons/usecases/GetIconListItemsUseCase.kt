package ch.rmy.android.http_shortcuts.activities.icons.usecases

import android.content.Context
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerListItem
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.IconUtil
import javax.inject.Inject

class GetIconListItemsUseCase
@Inject
constructor(
    private val context: Context,
    private val getUsedCustomIconsUseCase: GetUsedCustomIconsUseCase,
) {

    @CheckResult
    suspend operator fun invoke(): List<IconPickerListItem> {
        val usedIcons = getUsedCustomIconsUseCase(includeTemporaryShortcut = true)
        return IconUtil.getCustomIconNamesInApp(context)
            .map(ShortcutIcon::CustomIcon)
            .map { icon ->
                IconPickerListItem(icon, isUnused = !usedIcons.contains(icon))
            }
            .sortedWith(
                compareBy<IconPickerListItem> {
                    if (it.isUnused) 1 else 0
                }
                    .thenBy { it.icon.fileName },
            )
    }
}
