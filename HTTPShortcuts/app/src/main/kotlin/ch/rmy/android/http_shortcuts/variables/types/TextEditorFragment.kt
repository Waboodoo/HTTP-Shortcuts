package ch.rmy.android.http_shortcuts.variables.types

import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableEditorTextBinding

class TextEditorFragment : VariableEditorFragment<VariableEditorTextBinding>() {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorTextBinding.inflate(inflater, container, false)

    override fun updateViews(variable: Variable) {
        binding.inputRememberValue.isChecked = variable.rememberValue
        binding.inputMultiline.isChecked = variable.isMultiline
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = binding.inputRememberValue.isChecked
        variable.isMultiline = binding.inputMultiline.isChecked
    }
}
