package ch.rmy.android.http_shortcuts.activities.variables.editor.types.constant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.addArguments
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.WithValidation
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.databinding.VariableEditorConstantBinding
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import javax.inject.Inject

class ConstantTypeFragment : BaseFragment<VariableEditorConstantBinding>(), WithValidation {

    @Inject
    lateinit var variableViewUtils: VariableViewUtils

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
        variableViewUtils.bindVariableViews(binding.inputVariableValue, binding.variableButton, allowEditing = false)
    }

    private fun initUserInputBindings() {
        binding.inputVariableValue.doOnTextChanged {
            viewModel.onValueChanged(binding.inputVariableValue.rawString)
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputVariableValue.rawString = viewState.value
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun validate() {
        viewModel.onValidationEvent()
    }

    companion object {

        fun create(variableId: VariableId?): ConstantTypeFragment =
            ConstantTypeFragment().addArguments {
                putString(ARG_VARIABLE_ID, variableId)
            }

        private const val ARG_VARIABLE_ID = "variableId"
    }
}
