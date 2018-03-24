package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.adapters.VariableAdapter
import ch.rmy.android.http_shortcuts.dialogs.HelpDialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.ShortcutListDecorator
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import kotterknife.bindView

class VariablesActivity : BaseActivity() {

    private val variableList: RecyclerView by bindView(R.id.variable_list)
    private val createButton: FloatingActionButton by bindView(R.id.button_create_variable)

    private val controller by lazy { destroyer.own(Controller()) }

    private val clickedListener = object : OnItemClickedListener<Variable> {
        override fun onItemClicked(item: Variable) {
            editVariable(item)
        }

        override fun onItemLongClicked(item: Variable) {
            showContextMenu(item)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_variables)

        val adapter = destroyer.own(VariableAdapter(context))
        adapter.setItems(controller.getVariables())

        val manager = LinearLayoutManager(context)
        variableList.layoutManager = manager
        variableList.setHasFixedSize(true)
        variableList.addItemDecoration(ShortcutListDecorator(context, R.drawable.list_divider))
        variableList.adapter = adapter

        adapter.clickListener = clickedListener

        createButton.setOnClickListener { openEditorForCreation() }
    }

    private fun openEditorForCreation() {
        val intent = Intent(context, VariableEditorActivity::class.java)
        startActivity(intent)
    }

    private fun editVariable(variable: Variable) {
        val intent = VariableEditorActivity.IntentBuilder(context, variable.id)
                .build()
        startActivity(intent)
    }

    private fun showContextMenu(variable: Variable) {
        MenuDialogBuilder(context)
                .title(variable.key)
                .item(R.string.action_edit, {
                    editVariable(variable)
                })
                .item(R.string.action_delete, {
                    showDeleteDialog(variable)
                })
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
        showSnackbar(String.format(getString(R.string.variable_deleted), variable.key))
        controller.deleteVariable(variable.id)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variables_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_show_help) {
            showHelp()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showHelp() {
        HelpDialogBuilder(context)
                .title(R.string.help_title_variables)
                .message(R.string.help_variables)
                .build()
                .show()
                .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, VariablesActivity::class.java)

}
