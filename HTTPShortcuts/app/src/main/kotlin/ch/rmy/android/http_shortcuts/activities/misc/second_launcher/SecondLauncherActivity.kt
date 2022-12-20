package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import android.os.Bundle
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseActivity

class SecondLauncherActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    override val supportsSnackbars: Boolean
        get() = false

    private val viewModel: SecondLauncherViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        if (savedState == null) {
            viewModel.initialize()
        }
        initViewModelBindings()
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }
}
