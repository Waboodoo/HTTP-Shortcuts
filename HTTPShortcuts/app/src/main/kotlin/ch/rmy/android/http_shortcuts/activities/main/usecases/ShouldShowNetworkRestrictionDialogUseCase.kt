package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import javax.inject.Inject

class ShouldShowNetworkRestrictionDialogUseCase
@Inject
constructor(
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val networkUtil: NetworkUtil,
) {

    operator fun invoke() =
        networkUtil.isNetworkPerformanceRestricted() && !deviceLocalPreferences.isNetworkRestrictionWarningPermanentlyHidden
}
