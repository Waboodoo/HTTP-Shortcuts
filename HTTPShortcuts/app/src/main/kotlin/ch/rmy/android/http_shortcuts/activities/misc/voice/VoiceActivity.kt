package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.SearchManager
import android.os.Bundle
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseActivity

class VoiceActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    private val viewModel: VoiceViewModel by bindViewModel()

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(VoiceViewModel.InitData(intent.getStringExtra(SearchManager.QUERY)))
        initViewModelBindings()
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }
}
