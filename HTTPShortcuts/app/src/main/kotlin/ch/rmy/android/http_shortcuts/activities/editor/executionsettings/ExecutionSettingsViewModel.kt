package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.utils.StringUtils
import io.reactivex.Completable

class ExecutionSettingsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setWaitForConnection(waitForConnection: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.isWaitForNetwork = waitForConnection
        }

    fun setRequireConfirmation(requireConfirmation: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.requireConfirmation = requireConfirmation
        }

    fun setLauncherShortcut(launcherShortcut: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.launcherShortcut = launcherShortcut
        }

    fun setQuickSettingsTileShortcut(quickSettingsTileShortcut: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.quickSettingsTileShortcut = quickSettingsTileShortcut
        }

    fun setDelay(delay: Int): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.delay = delay
        }

    fun getDelaySubtitle(shortcut: Shortcut): CharSequence =
        getDelayText(shortcut.delay)

    fun getDelayText(delay: Int) =
        StringUtils.getDurationText(context, delay)

}