package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.usecases.GetAvailableBrowserPackageNamesUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.type
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BasicRequestSettingsViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val getAvailableBrowserPackageNames: GetAvailableBrowserPackageNamesUseCase,
) : BaseViewModel<Unit, BasicRequestSettingsViewState>(application) {

    override suspend fun initialize(data: Unit): BasicRequestSettingsViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        return BasicRequestSettingsViewState(
            methodVisible = shortcut.type == ShortcutExecutionType.APP,
            browserPackageNameVisible = shortcut.type == ShortcutExecutionType.BROWSER,
            method = shortcut.method,
            url = shortcut.url,
            browserPackageName = shortcut.browserPackageName,
            browserPackageNameOptions = if (shortcut.type == ShortcutExecutionType.BROWSER) {
                getAvailableBrowserPackageNames(shortcut.browserPackageName)
            } else {
                emptyList()
            },
        )
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    fun onUrlChanged(url: String) = runAction {
        updateViewState {
            copy(url = url)
        }
        withProgressTracking {
            temporaryShortcutRepository.setUrl(url)
        }
    }

    fun onMethodChanged(method: String) = runAction {
        updateViewState {
            copy(method = method)
        }
        withProgressTracking {
            temporaryShortcutRepository.setMethod(method)
        }
    }

    fun onBrowserPackageNameChanged(packageName: String) = runAction {
        updateViewState {
            copy(browserPackageName = packageName)
        }
        withProgressTracking {
            temporaryShortcutRepository.setBrowserPackageName(packageName)
        }
    }
}
