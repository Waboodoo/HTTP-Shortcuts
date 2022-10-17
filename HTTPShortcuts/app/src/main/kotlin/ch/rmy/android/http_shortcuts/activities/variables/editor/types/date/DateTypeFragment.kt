package ch.rmy.android.http_shortcuts.activities.variables.editor.types.date

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
import ch.rmy.android.http_shortcuts.databinding.VariableEditorDateBinding

class DateTypeFragment : BaseFragment<VariableEditorDateBinding>() {

    private val viewModel: DateTypeViewModel by bindViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorDateBinding.inflate(inflater, container, false)

    override fun setupViews() {
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initUserInputBindings() {
        binding.inputVariableDateFormat.doOnTextChanged {
            viewModel.onDateFormatChanged(it.toString())
        }

        binding.inputRememberValue.doOnCheckedChanged(viewModel::onRememberValueChanged)
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.inputVariableDateFormat.setTextSafely(viewState.dateFormat)
            binding.inputRememberValue.isChecked = viewState.rememberValue
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }
}
