package ch.rmy.android.http_shortcuts.activities.settings.about

import android.app.Application
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.http_shortcuts.usecases.GetChangeLogDialogUseCase
import ch.rmy.android.http_shortcuts.utils.Settings

class AboutViewModel(application: Application) : BaseViewModel<Unit, AboutViewState>(application), WithDialog {

    private val settings = Settings(context)
    private val getChangeLogDialog = GetChangeLogDialogUseCase(settings)

    override fun initViewState() = AboutViewState()

    override fun onDialogDismissed(id: String?) {
        updateViewState {
            copy(dialogState = null)
        }
    }

    fun onChangeLogButtonClicked() {
        updateViewState {
            copy(dialogState = getChangeLogDialog())
        }
    }
}
