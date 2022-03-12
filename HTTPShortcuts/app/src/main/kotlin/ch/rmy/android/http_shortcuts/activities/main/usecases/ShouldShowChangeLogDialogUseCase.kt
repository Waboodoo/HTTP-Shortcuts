package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil.getVersion

class ShouldShowChangeLogDialogUseCase(
    private val context: Context,
) {

    private val settings: Settings = Settings(context)

    @CheckResult
    operator fun invoke(): Boolean {
        val lastSeenVersion = settings.changeLogLastVersion
        if (isPermanentlyHidden && hasSeenAllImportantVersions(lastSeenVersion)) {
            return false
        }
        val version = getVersion(context)
        settings.changeLogLastVersion = version
        return lastSeenVersion != null && version != lastSeenVersion
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden

    private fun hasSeenAllImportantVersions(lastSeenVersion: Long?) =
        IMPORTANT_VERSIONS.all { it <= (lastSeenVersion ?: 0L) }

    companion object {
        private val IMPORTANT_VERSIONS = setOf(
            204,
        )
    }
}
