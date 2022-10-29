package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import javax.inject.Inject

class ShouldShowNetworkRestrictionDialogUseCase
@Inject
constructor(
    private val settings: Settings,
    private val networkUtil: NetworkUtil,
) {

    operator fun invoke() =
        networkUtil.isNetworkPerformanceRestricted() && !settings.isNetworkRestrictionWarningPermanentlyHidden
}
