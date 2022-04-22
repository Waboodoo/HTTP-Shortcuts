package ch.rmy.android.http_shortcuts.activities.variables.editor.types.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.ui.BaseFragment
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
        binding.inputRememberValue
            .observeChecked()
            .subscribe(viewModel::onRememberValueChanged)
            .attachTo(destroyer)

        binding.inputMultiline
            .observeChecked()
            .subscribe(viewModel::onMultilineChanged)
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.inputRememberValue.isChecked = viewState.rememberValue
            binding.inputMultiline.isVisible = viewState.isMultilineCheckboxVisible
            binding.inputMultiline.isChecked = viewState.isMultiline
        }
        viewModel.events.observe(this, ::handleEvent)
    }
}
