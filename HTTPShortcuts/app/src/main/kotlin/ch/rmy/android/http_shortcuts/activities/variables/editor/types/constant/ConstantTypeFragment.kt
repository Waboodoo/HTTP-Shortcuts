package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.addArguments
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.ui.BaseFragment
import ch.rmy.android.http_shortcuts.databinding.VariableEditorConstantBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class ConstantTypeFragment private constructor() : BaseFragment<VariableEditorConstantBinding>() {

    private val viewModel: ConstantTypeViewModel by bindViewModel()

    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorConstantBinding.inflate(inflater, container, false)

    override fun setupViews() {
        viewModel.initialize(
            ConstantTypeViewModel.InitData(
                variableId = args.getString(ARG_VARIABLE_ID),
            )
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        bindVariableViews(binding.inputVariableValue, binding.variableButton, variablePlaceholderProvider, allowEditing = false)
            .attachTo(destroyer)
    }

    private fun initUserInputBindings() {
        binding.inputVariableValue
            .observeTextChanges()
            .subscribe {
                viewModel.onValueChanged(binding.inputVariableValue.rawString)
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.inputVariableValue.rawString = viewState.value
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    companion object {

        fun create(variableId: String?): ConstantTypeFragment =
            ConstantTypeFragment().addArguments {
                putString(ARG_VARIABLE_ID, variableId)
            }

        private const val ARG_VARIABLE_ID = "variableId"
    }
}
