package ch.rmy.android.http_shortcuts.activities.icons.usecases

import android.content.Context
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerListItem
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.usecases.GetUsedCustomIconsUseCase
import ch.rmy.android.http_shortcuts.utils.IconUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GetIconListItemsUseCase(
    private val context: Context,
    private val getUsedCustomIconsUseCase: GetUsedCustomIconsUseCase,
) {

    @CheckResult
    operator fun invoke(): Single<List<IconPickerListItem>> =
        getUsedCustomIconsUseCase(includeTemporaryShortcut = true)
            .map { usedIcons ->
                IconUtil.getCustomIconNamesInApp(context)
                    .map {
                        ShortcutIcon.CustomIcon(it)
                    }
                    .map { icon ->
                        IconPickerListItem(icon, isUnused = !usedIcons.contains(icon))
                    }
                    .sortedWith(
                        compareBy<IconPickerListItem> {
                            if (it.isUnused) 1 else 0
                        }
                            .thenBy { it.icon.fileName }
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}
