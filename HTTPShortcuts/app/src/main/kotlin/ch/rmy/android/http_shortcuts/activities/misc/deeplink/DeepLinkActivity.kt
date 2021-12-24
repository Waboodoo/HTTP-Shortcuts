package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseActivity

class DeepLinkActivity : BaseActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    private val viewModel: DeepLinkViewModel by bindViewModel()

    override fun onCreate() {
        viewModel.initialize(DeepLinkViewModel.InitData(intent.data))
        initViewModelBindings()
    }

    private fun initViewModelBindings() {
        viewModel.events.observe(this, ::handleEvent)
    }
}
