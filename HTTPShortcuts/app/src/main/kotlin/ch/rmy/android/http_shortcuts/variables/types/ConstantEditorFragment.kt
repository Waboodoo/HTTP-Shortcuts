package ch.rmy.android.http_shortcuts.variables.types

import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableEditorConstantBinding
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class ConstantEditorFragment : VariableEditorFragment<VariableEditorConstantBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorConstantBinding.inflate(inflater, container, false)

    override fun setupViews() {
        bindVariableViews(binding.inputVariableValue, binding.variableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    override fun updateViews(variable: Variable) {
        binding.inputVariableValue.rawString = variable.value ?: ""
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.value = binding.inputVariableValue.rawString
    }
}
