package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil.getVersionName
import javax.inject.Inject

class ShouldShowChangeLogDialogUseCase
@Inject
constructor(
    private val context: Context,
    private val settings: Settings,
) {
    @CheckResult
    operator fun invoke(): Boolean {
        val lastSeenVersion = settings.changeLogLastVersion
        if (isPermanentlyHidden) {
            return false
        }
        val version = getVersionName(context)
        settings.changeLogLastVersion = version
        return lastSeenVersion != null && version != lastSeenVersion
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden
}
