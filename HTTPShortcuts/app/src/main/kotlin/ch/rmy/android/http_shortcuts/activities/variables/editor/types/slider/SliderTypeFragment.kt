package ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.databinding.VariableEditorSliderBinding

class SliderTypeFragment : BaseFragment<VariableEditorSliderBinding>() {

    private val viewModel: SliderTypeViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorSliderBinding.inflate(inflater, container, false)

    override fun setupViews() {
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initUserInputBindings() {
        binding.inputRememberValue.doOnCheckedChanged(viewModel::onRememberValueChanged)

        binding.inputSliderMin.doOnTextChanged {
            viewModel.onMinValueChanged(it.toString())
        }
        binding.inputSliderMax.doOnTextChanged {
            viewModel.onMaxValueChanged(it.toString())
        }
        binding.inputSliderStep.doOnTextChanged {
            viewModel.onStepSizeChanged(it.toString())
        }
        binding.inputSliderPrefix.doOnTextChanged {
            viewModel.onPrefixChanged(it.toString())
        }
        binding.inputSliderSuffix.doOnTextChanged {
            viewModel.onSuffixChanged(it.toString())
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputSliderMin.setTextSafely(viewState.minValueText)
            binding.inputSliderMax.setTextSafely(viewState.maxValueText)
            binding.inputSliderStep.setTextSafely(viewState.stepSizeText)
            binding.inputSliderPrefix.setTextSafely(viewState.prefix)
            binding.inputSliderSuffix.setTextSafely(viewState.suffix)
            binding.inputRememberValue.isChecked = viewState.rememberValue
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }
}
