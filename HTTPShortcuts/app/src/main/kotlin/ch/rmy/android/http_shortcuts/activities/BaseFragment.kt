package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import ch.rmy.android.framework.ui.BaseFragment
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent

abstract class BaseFragment<Binding : ViewBinding> : BaseFragment<Binding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject(requireContext().getApplicationComponent())
    }

    protected open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }
}
