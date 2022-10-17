package ch.rmy.android.http_shortcuts.activities.variables.editor.types.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.databinding.VariableEditorTextBinding

class TextTypeFragment : BaseFragment<VariableEditorTextBinding>() {

    private val viewModel: TextTypeViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorTextBinding.inflate(inflater, container, false)

    override fun setupViews() {
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initUserInputBindings() {
        binding.inputRememberValue.doOnCheckedChanged(viewModel::onRememberValueChanged)
        binding.inputMultiline.doOnCheckedChanged(viewModel::onMultilineChanged)
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputRememberValue.isChecked = viewState.rememberValue
            binding.inputMultiline.isVisible = viewState.isMultilineCheckboxVisible
            binding.inputMultiline.isChecked = viewState.isMultiline
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }
}
