package ch.rmy.android.http_shortcuts.activities.variables.editor.types.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.databinding.SelectOptionEditorItemBinding
import ch.rmy.android.http_shortcuts.databinding.VariableEditorSelectBinding
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import javax.inject.Inject

class SelectTypeFragment : BaseFragment<VariableEditorSelectBinding>() {

    @Inject
    lateinit var variableViewUtils: VariableViewUtils

    @Inject
    lateinit var adapter: SelectVariableOptionsAdapter

    private val viewModel: SelectTypeViewModel by bindViewModel()

    private var isDraggingEnabled = false

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initialize()
    }

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorSelectBinding.inflate(inflater, container, false)

    override fun setupViews() {
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding.selectOptionsList.layoutManager = LinearLayoutManager(context)
        binding.selectOptionsList.adapter = adapter
    }

    private fun initUserInputBindings() {
        initDragOrdering()
        adapter.userEvents.observe(this) { event ->
            when (event) {
                is SelectVariableOptionsAdapter.UserEvent.OptionClicked -> viewModel.onOptionClicked(event.id)
            }
        }

        binding.selectOptionsAddButton.setOnClickListener {
            viewModel.onAddButtonClicked()
        }
        binding.inputMultiSelect
            .observeChecked()
            .subscribe(viewModel::onMultiSelectChanged)
            .attachTo(destroyer)
        binding.inputSeparator
            .observeTextChanges()
            .subscribe {
                viewModel.onSeparatorChanged(it.toString())
            }
            .attachTo(destroyer)
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? SelectVariableOptionsAdapter.SelectOptionViewHolder)?.optionId },
        )
        dragOrderingHelper.movementSource
            .subscribe { (optionId1, optionId2) ->
                viewModel.onOptionMoved(optionId1, optionId2)
            }
            .attachTo(destroyer)
        dragOrderingHelper.attachTo(binding.selectOptionsList)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.options
            binding.inputSeparator.setTextSafely(viewState.separator)
            binding.inputSeparator.isEnabled = viewState.separatorEnabled
            binding.inputMultiSelect.isChecked = viewState.isMultiSelect
            isDraggingEnabled = viewState.isDraggingEnabled
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is SelectTypeEvent.ShowAddDialog -> showAddDialog()
            is SelectTypeEvent.ShowEditDialog -> showEditDialog(event.optionId, event.label, event.value)
            else -> super.handleEvent(event)
        }
    }

    private fun showAddDialog() {
        val binding = SelectOptionEditorItemBinding.inflate(layoutInflater)
        variableViewUtils.bindVariableViews(binding.selectOptionValue, binding.variableButtonValue)
        DialogBuilder(requireContext())
            .title(R.string.title_add_select_option)
            .view(binding.root)
            .positive(R.string.dialog_ok) {
                viewModel.onAddDialogConfirmed(
                    label = binding.selectOptionLabel.text.toString(),
                    value = binding.selectOptionValue.rawString,
                )
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun showEditDialog(optionId: String, label: String, value: String) {
        val binding = SelectOptionEditorItemBinding.inflate(layoutInflater)
        variableViewUtils.bindVariableViews(binding.selectOptionValue, binding.variableButtonValue)
        binding.selectOptionLabel.setText(label)
        binding.selectOptionValue.rawString = value
        DialogBuilder(requireContext())
            .title(R.string.title_edit_select_option)
            .view(binding.root)
            .positive(R.string.dialog_ok) {
                viewModel.onEditDialogConfirmed(
                    optionId = optionId,
                    label = binding.selectOptionLabel.text.toString(),
                    value = binding.selectOptionValue.rawString,
                )
            }
            .negative(R.string.dialog_cancel)
            .neutral(R.string.dialog_remove) {
                viewModel.onDeleteOptionSelected(optionId)
            }
            .showIfPossible()
    }
}
