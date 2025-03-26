package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.usecases.GetAvailableBrowserPackageNamesUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
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
        val type = shortcut.executionType
        return BasicRequestSettingsViewState(
            shortcutExecutionType = type,
            method = shortcut.method,
            url = shortcut.url,
            targetBrowser = shortcut.targetBrowser,
            browserPackageNameOptions = if (type == ShortcutExecutionType.BROWSER) {
                getAvailableBrowserPackageNames(shortcut.targetBrowser.packageName)
            } else {
                emptyList()
            },
            wolMacAddress = shortcut.wolMacAddress,
            wolPort = shortcut.wolPort.toString(),
            wolBroadcastAddress = shortcut.wolBroadcastAddress,
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

    fun onMethodChanged(method: HttpMethod) = runAction {
        updateViewState {
            copy(method = method)
        }
        withProgressTracking {
            temporaryShortcutRepository.setMethod(method)
        }
    }

    fun onTargetBrowserChanged(targetBrowser: TargetBrowser) = runAction {
        updateViewState {
            copy(targetBrowser = targetBrowser)
        }
        withProgressTracking {
            temporaryShortcutRepository.setTargetBrowser(targetBrowser)
        }
    }

    fun onWolMacAddressChanged(macAddress: String) = runAction {
        updateViewState {
            copy(wolMacAddress = macAddress)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWolMacAddress(macAddress)
        }
    }

    fun onWolPortChanged(port: String) = runAction {
        updateViewState {
            copy(wolPort = port)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWolPort(port.toIntOrNull() ?: 9)
        }
    }

    fun onWolBroadcastAddressChanged(broadcastAddress: String) = runAction {
        updateViewState {
            copy(wolBroadcastAddress = broadcastAddress)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWolBroadcastAddress(broadcastAddress)
        }
    }
}
