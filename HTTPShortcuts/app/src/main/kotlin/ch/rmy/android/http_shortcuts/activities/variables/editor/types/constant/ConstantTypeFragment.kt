package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.addArguments
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.databinding.VariableEditorConstantBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews
import javax.inject.Inject

class ConstantTypeFragment private constructor() : BaseFragment<VariableEditorConstantBinding>() {

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    private val viewModel: ConstantTypeViewModel by bindViewModel()

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize(
            ConstantTypeViewModel.InitData(
                variableId = args.getString(ARG_VARIABLE_ID),
            )
        )
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorConstantBinding.inflate(inflater, container, false)

    override fun setupViews() {
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
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.inputVariableValue.rawString = viewState.value
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    companion object {

        fun create(variableId: VariableId?): ConstantTypeFragment =
            ConstantTypeFragment().addArguments {
                putString(ARG_VARIABLE_ID, variableId)
            }

        private const val ARG_VARIABLE_ID = "variableId"
    }
}
