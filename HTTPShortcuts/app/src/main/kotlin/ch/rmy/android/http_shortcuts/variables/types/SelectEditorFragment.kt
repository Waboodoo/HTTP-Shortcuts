package ch.rmy.android.http_shortcuts.variables.types

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.SelectVariableOptionsAdapter
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableEditorSelectBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class SelectEditorFragment : VariableEditorFragment<VariableEditorSelectBinding>() {

    private var variable: Variable? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorSelectBinding.inflate(inflater, container, false)

    private val optionsAdapter = SelectVariableOptionsAdapter()

    override fun setupViews() {
        binding.selectOptionsAddButton.setOnClickListener { showAddDialog() }
        binding.selectOptionsList.layoutManager = LinearLayoutManager(context)
        binding.selectOptionsList.adapter = optionsAdapter
        optionsAdapter.clickListener = ::showEditDialog
        initDragOrdering()

        binding.inputMultiSelect.setOnCheckedChangeListener { _, _ ->
            compileIntoVariable(variable!!)
            updateViews(variable!!)
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { variable!!.options!!.size > 1 }
        dragOrderingHelper.positionChangeSource
            .subscribe { (oldPosition, newPosition) ->
                variable!!.options!!.move(oldPosition, newPosition)
                optionsAdapter.notifyItemMoved(oldPosition, newPosition)
            }
            .attachTo(destroyer)
        dragOrderingHelper.attachTo(binding.selectOptionsList)
    }

    override fun updateViews(variable: Variable) {
        this.variable = variable
        optionsAdapter.options = variable.options!!
        val isMultiSelect = SelectType.isMultiSelect(variable)
        binding.inputSeparator.setText(SelectType.getSeparator(variable))
        binding.inputSeparator.isEnabled = isMultiSelect
        binding.inputMultiSelect.isChecked = isMultiSelect
        optionsAdapter.notifyDataSetChanged()
    }

    private fun showAddDialog() {
        showEditDialog(null)
    }

    private fun showEditDialog(option: Option?) {
        val destroyer = Destroyer()

        val editorView = layoutInflater.inflate(R.layout.select_option_editor_item, null)
        val labelInput = editorView.findViewById<EditText>(R.id.select_option_label)
        val valueInput = editorView.findViewById<VariableEditText>(R.id.select_option_value)
        val valueVariableButton = editorView.findViewById<VariableButton>(R.id.variable_button_value)

        bindVariableViews(valueInput, valueVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)

        if (option != null) {
            labelInput.setText(option.label)
            valueInput.rawString = option.value
        }

        DialogBuilder(requireContext())
            .title(if (option != null) R.string.title_edit_select_option else R.string.title_add_select_option)
            .view(editorView)
            .positive(R.string.dialog_ok) {
                val label = labelInput.text.toString()
                val value = valueInput.rawString
                if (option != null) {
                    updateOption(option, label, value)
                } else {
                    addNewOption(label, value)
                }
            }
            .negative(R.string.dialog_cancel)
            .mapIfNotNull(option) { persistedOption ->
                neutral(R.string.dialog_remove) { removeOption(persistedOption) }
            }
            .dismissListener {
                destroyer.destroy()
            }
            .showIfPossible()
    }

    private fun addNewOption(label: String, value: String) {
        val option = Option(label = label, value = value)
        variable!!.options!!.add(option)
        updateViews(variable!!)
    }

    private fun updateOption(option: Option, label: String, value: String) {
        option.label = label
        option.value = value
        updateViews(variable!!)
    }

    private fun removeOption(option: Option) {
        variable!!.options!!.removeAll { it.id == option.id }
        updateViews(variable!!)
    }

    override fun validate() = if (variable!!.options!!.isEmpty()) {
        showMessageDialog(R.string.error_not_enough_select_values)
        false
    } else {
        true
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.dataForType = mapOf(
            SelectType.KEY_MULTI_SELECT to binding.inputMultiSelect.isChecked.toString(),
            SelectType.KEY_SEPARATOR to binding.inputSeparator.text.toString(),
        )
    }

}
