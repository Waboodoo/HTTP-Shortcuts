package ch.rmy.android.http_shortcuts.activities.variables

import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.mapFor
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityVariablesBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.applyTheme

class VariablesActivity : BaseActivity() {

    private val viewModel: VariablesViewModel by bindViewModel()

    private lateinit var binding: ActivityVariablesBinding
    private lateinit var adapter: VariableAdapter

    private var isDraggingEnabled = false

    override fun onCreate() {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityVariablesBinding.inflate(layoutInflater))
        setTitle(R.string.title_variables)

        adapter = VariableAdapter()
        val manager = LinearLayoutManager(context)
        binding.variableList.layoutManager = manager
        binding.variableList.setHasFixedSize(true)
        binding.variableList.adapter = adapter

        binding.buttonCreateVariable.applyTheme(themeHelper)
    }

    private fun initUserInputBindings() {
        initDragOrdering()

        adapter.userEvents.observe(this) { event ->
            when (event) {
                is VariableAdapter.UserEvent.VariableClicked -> viewModel.onVariableClicked(event.id)
            }
        }
        binding.buttonCreateVariable.setOnClickListener {
            viewModel.onCreateButtonClicked()
        }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper(
            isEnabledCallback = { isDraggingEnabled },
            getId = { (it as? VariableAdapter.VariableViewHolder)?.variableId },
        )
        dragOrderingHelper.attachTo(binding.variableList)
        dragOrderingHelper.movementSource.observe(this) { (variableId1, variableId2) ->
            viewModel.onVariableMoved(variableId1, variableId2)
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.variables
            isDraggingEnabled = viewState.isDraggingEnabled
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is VariablesEvent.ShowCreationDialog -> {
                showCreationDialog(event.variableOptions)
            }
            is VariablesEvent.ShowContextMenu -> {
                showContextMenu(event.variableId, event.title)
            }
            is VariablesEvent.ShowDeletionDialog -> {
                showDeletionDialog(event.variableId, event.title, event.message)
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showCreationDialog(variableOptions: List<VariablesEvent.ShowCreationDialog.VariableTypeOption>) {
        DialogBuilder(context)
            .title(R.string.title_select_variable_type)
            .mapFor(variableOptions) { option ->
                when (option) {
                    is VariablesEvent.ShowCreationDialog.VariableTypeOption.Separator -> separator()
                    is VariablesEvent.ShowCreationDialog.VariableTypeOption.Variable -> {
                        item(name = option.name.localize(context)) {
                            viewModel.onCreationDialogVariableTypeSelected(option.type)
                        }
                    }
                }
            }
            .showIfPossible()
    }

    private fun showContextMenu(variableId: String, title: Localizable) {
        DialogBuilder(context)
            .title(title)
            .item(R.string.action_edit) {
                viewModel.onEditOptionSelected(variableId)
            }
            .item(R.string.action_duplicate) {
                viewModel.onDuplicateOptionSelected(variableId)
            }
            .item(R.string.action_delete) {
                viewModel.onDeletionOptionSelected(variableId)
            }
            .showIfPossible()
    }

    private fun showDeletionDialog(variableId: String, title: String, message: Localizable) {
        DialogBuilder(context)
            .title(title)
            .message(message)
            .positive(R.string.dialog_delete) {
                viewModel.onDeletionConfirmed(variableId)
            }
            .negative(R.string.dialog_cancel)
            .showIfPossible()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variables_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(VariablesActivity::class.java)
}
