package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.VariableEditorActivity
import ch.rmy.android.http_shortcuts.adapters.VariableAdapter
import ch.rmy.android.http_shortcuts.dialogs.HelpDialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.utils.attachTo
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotterknife.bindView

class VariablesActivity : BaseActivity() {

    private val viewModel: VariablesViewModel by lazy {
        ViewModelProviders.of(this).get(VariablesViewModel::class.java)
    }

    // Views
    private val variableList: RecyclerView by bindView(R.id.variable_list)
    private val createButton: FloatingActionButton by bindView(R.id.button_create_variable)

    private val variables by lazy { viewModel.getVariables() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_variables)

        val adapter = destroyer.own(VariableAdapter(context, variables))
        adapter.clickListener = this::showContextMenu

        val manager = LinearLayoutManager(context)
        variableList.layoutManager = manager
        variableList.setHasFixedSize(true)
        variableList.adapter = adapter

        initDragOrdering()

        createButton.setOnClickListener { openEditorForCreation() }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { variables.size > 1 }
        dragOrderingHelper.positionChangeSource
            .add { (oldPosition, newPosition) ->
                val variable = variables[oldPosition]!!
                viewModel.moveVariable(variable.id, newPosition)
                    .subscribe()
                    .attachTo(destroyer)
            }
            .attachTo(destroyer)
        dragOrderingHelper.attachTo(variableList)
    }

    private fun openEditorForCreation() {
        val intent = VariableEditorActivity.IntentBuilder(context)
            .build()
        startActivity(intent)
    }

    private fun editVariable(variable: Variable) {
        val intent = VariableEditorActivity.IntentBuilder(context)
            .variableId(variable.id)
            .build()
        startActivity(intent)
    }

    private fun showContextMenu(variable: Variable) {
        MenuDialogBuilder(context)
            .title(variable.key)
            .item(R.string.action_edit) {
                editVariable(variable)
            }
            .item(R.string.action_delete) {
                showDeleteDialog(variable)
            }
            .showIfPossible()
    }

    private fun showDeleteDialog(variable: Variable) {
        MaterialDialog.Builder(context)
            .content(R.string.confirm_delete_variable_message)
            .positiveText(R.string.dialog_delete)
            .onPositive { _, _ -> deleteVariable(variable) }
            .negativeText(R.string.dialog_cancel)
            .showIfPossible()
    }

    private fun deleteVariable(variable: Variable) {
        val key = variable.key
        viewModel.deleteVariable(variable.id)
            .subscribe {
                showSnackbar(String.format(getString(R.string.variable_deleted), key))
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variables_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { showHelp() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showHelp() {
        HelpDialogBuilder(context)
            .title(R.string.help_title_variables)
            .message(R.string.help_variables)
            .build()
            .show()
            .attachTo(destroyer)
    }

    override fun onStart() {
        super.onStart()
        showHelpIfNeeded()
    }

    private fun showHelpIfNeeded() {
        if (!viewModel.wasVariableIntroShown) {
            showHelp()
            viewModel.wasVariableIntroShown = true
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, VariablesActivity::class.java)

}
