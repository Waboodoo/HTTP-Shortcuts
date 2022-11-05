package ch.rmy.android.http_shortcuts.activities.variables.editor.types.color

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.WithValidation
import ch.rmy.android.http_shortcuts.databinding.VariableEditorColorBinding

class ColorTypeFragment : BaseFragment<VariableEditorColorBinding>(), WithValidation {

    private val viewModel: ColorTypeViewModel by bindViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorColorBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun setupViews() {
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initUserInputBindings() {
        binding.inputRememberValue.doOnCheckedChanged(viewModel::onRememberValueChanged)
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputRememberValue.isChecked = viewState.rememberValue
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun validate() {
        viewModel.onValidationEvent()
    }
}
