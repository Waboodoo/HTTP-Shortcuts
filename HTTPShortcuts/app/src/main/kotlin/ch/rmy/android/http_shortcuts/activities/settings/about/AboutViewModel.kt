package ch.rmy.android.http_shortcuts.activities.settings.about

import android.app.Application
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.usecases.GetChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.utils.Settings

class AboutViewModel(application: Application) : BaseViewModel<Unit, AboutViewState>(application), WithDialog {

    private val settings = Settings(context)
    private val getChangeLogDialog = GetChangeLogDialogUseCase(settings)

    override var dialogState: DialogState?
        get() = currentViewState.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun initViewState() = AboutViewState()

    fun onChangeLogButtonClicked() {
        updateViewState {
            copy(dialogState = getChangeLogDialog())
        }
    }
}
