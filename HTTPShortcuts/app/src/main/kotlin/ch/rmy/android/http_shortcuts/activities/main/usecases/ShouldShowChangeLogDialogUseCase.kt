package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import androidx.annotation.CheckResult
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.VersionUtil.getVersion
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
        /**
         * Contains all version numbers which are considered important enough that they may force the displaying of the changelog dialog, even when
         * the user has set it to never appear automatically on updates. This can be used to announce important (e.g. breaking) changes.
         */
        private val IMPORTANT_VERSIONS = setOf(
            204,
        )
    }
}
