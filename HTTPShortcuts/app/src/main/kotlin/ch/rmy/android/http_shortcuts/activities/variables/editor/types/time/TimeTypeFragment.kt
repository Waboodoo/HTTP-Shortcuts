package ch.rmy.android.http_shortcuts.activities.variables.editor.types.time

import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseFragment
import ch.rmy.android.http_shortcuts.databinding.VariableEditorTimeBinding

class TimeTypeFragment : BaseFragment<VariableEditorTimeBinding>() {

    private val viewModel: TimeTypeViewModel by bindViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorTimeBinding.inflate(inflater, container, false)

    override fun setupViews() {
        viewModel.initialize()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initUserInputBindings() {
        binding.inputVariableTimeFormat
            .observeTextChanges()
            .subscribe {
                viewModel.onTimeFormatChanged(it.toString())
            }
            .attachTo(destroyer)

        binding.inputRememberValue
            .observeChecked()
            .subscribe(viewModel::onRememberValueChanged)
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.inputVariableTimeFormat.setTextSafely(viewState.timeFormat)
            binding.inputRememberValue.isChecked = viewState.rememberValue
        }
        viewModel.events.observe(this, ::handleEvent)
    }
}
