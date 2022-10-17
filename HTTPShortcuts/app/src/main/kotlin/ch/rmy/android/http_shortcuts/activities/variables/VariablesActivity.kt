package ch.rmy.android.http_shortcuts.activities.variables

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.whileLifecycleActive
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityVariablesBinding
import ch.rmy.android.http_shortcuts.extensions.applyTheme

class VariablesActivity : BaseActivity() {

    private val viewModel: VariablesViewModel by bindViewModel()

    private lateinit var binding: ActivityVariablesBinding
    private lateinit var adapter: VariableAdapter
    private var sortMenuItem: MenuItem? = null

    private var isDraggingEnabled = false

    override fun onCreated(savedState: Bundle?) {
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

        whileLifecycleActive {
            adapter.userEvents.collect { event ->
                when (event) {
                    is VariableAdapter.UserEvent.VariableClicked -> viewModel.onVariableClicked(event.id)
                }
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
        whileLifecycleActive {
            dragOrderingHelper.movementSource.collect { (variableId1, variableId2) ->
                viewModel.onVariableMoved(variableId1, variableId2)
            }
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.loadingIndicator.isVisible = false
            binding.buttonCreateVariable.isVisible = true
            adapter.items = viewState.variables
            isDraggingEnabled = viewState.isDraggingEnabled
            sortMenuItem?.isEnabled = viewState.isSortButtonEnabled
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variables_activity_menu, menu)
        sortMenuItem = menu.findItem(R.id.action_sort)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        R.id.action_sort -> consume { viewModel.onSortButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(VariablesActivity::class)
}
