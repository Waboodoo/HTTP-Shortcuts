package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.Settings

class ShouldShowNetworkRestrictionDialogUseCase(
    private val context: Context,
    private val settings: Settings,
) {

    operator fun invoke() =
        NetworkUtil.isNetworkPerformanceRestricted(context) && !settings.isNetworkRestrictionWarningPermanentlyHidden
}
