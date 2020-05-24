package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.HelpDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView

class VariablesActivity : BaseActivity() {

    private val viewModel: VariablesViewModel by bindViewModel()

    // Views
    private val variableList: RecyclerView by bindView(R.id.variable_list)
    private val createButton: FloatingActionButton by bindView(R.id.button_create_variable)

    private val variables by lazy { viewModel.getVariables() }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_variables)
        setTitle(R.string.title_variables)

        val adapter = destroyer.own(VariableAdapter(context, variables))
        adapter.clickListener = ::showContextMenu

        val manager = LinearLayoutManager(context)
        variableList.layoutManager = manager
        variableList.setHasFixedSize(true)
        variableList.adapter = adapter

        initDragOrdering()

        createButton.applyTheme(themeHelper)
        createButton.setOnClickListener { openEditorForCreation() }
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { variables.size > 1 }
        dragOrderingHelper.positionChangeSource
            .subscribe { (oldPosition, newPosition) ->
                val variable = variables[oldPosition]!!
                viewModel.moveVariable(variable.id, newPosition)
                    .subscribe()
                    .attachTo(destroyer)
            }
            .attachTo(destroyer)
        dragOrderingHelper.attachTo(variableList)
    }

    private fun openEditorForCreation() {
        DialogBuilder(context)
            .title(R.string.title_select_variable_type)
            .mapFor(VariableTypes.TYPES) { builder, (type, stringRes) ->
                builder.item(stringRes) {
                    VariableEditorActivity.IntentBuilder(context)
                        .variableType(type)
                        .build()
                        .startActivity(this)
                }
            }
            .showIfPossible()
    }


    private fun showContextMenu(variableData: LiveData<Variable?>) {
        val variable = variableData.value ?: return
        DialogBuilder(context)
            .title(variable.key)
            .item(R.string.action_edit) {
                editVariable(variable)
            }
            .item(R.string.action_duplicate) {
                duplicateVariable(variable)
            }
            .item(R.string.action_delete) {
                beginDeletion(variableData.value ?: return@item)
            }
            .showIfPossible()
    }

    private fun editVariable(variable: Variable) {
        VariableEditorActivity.IntentBuilder(context)
            .variableId(variable.id)
            .build()
            .startActivity(this)
    }

    private fun duplicateVariable(variable: Variable) {
        val key = variable.key
        viewModel.duplicateVariable(variable.id)
            .subscribe({
                showSnackbar(getString(R.string.message_variable_duplicated).format(key))
            }, {
                showSnackbar(R.string.error_generic, long = true)
            })
            .attachTo(destroyer)
    }

    private fun beginDeletion(variable: Variable) {
        val variableId = variable.id
        val variableKey = variable.key

        getShortcutNamesWhereVariableIsInUse(variableId)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { shortcutNames ->
                    val message = getString(R.string.confirm_delete_variable_message)
                        .mapIf(shortcutNames.isNotEmpty()) {
                            it.plus("\n\n")
                                .plus(context.resources.getQuantityString(
                                    R.plurals.warning_variable_still_in_use_in_shortcuts,
                                    shortcutNames.size,
                                    shortcutNames.joinToString(),
                                    shortcutNames.size
                                ))
                        }
                    DialogBuilder(context)
                        .message(message)
                        .positive(R.string.dialog_delete) {
                            deleteVariable(variableId, variableKey)
                        }
                        .negative(R.string.dialog_cancel)
                        .showIfPossible()
                },
                { error ->
                    logException(error)
                    showSnackbar(R.string.error_generic)
                }
            )
            .attachTo(destroyer)
    }

    // TODO: Also check if the variable is used inside another variable
    private fun getShortcutNamesWhereVariableIsInUse(variableId: String): Single<List<String>> =
        Single.fromCallable {
            Controller().use { controller ->
                val variableLookup = VariableManager(controller.getVariables())
                controller.getShortcuts().filter { shortcut ->
                    VariableResolver.extractVariableIds(shortcut, variableLookup)
                        .contains(variableId)
                }
                    .map {
                        it.name
                    }
                    .distinct()
            }
        }
            .subscribeOn(Schedulers.computation())

    private fun deleteVariable(variableId: String, variableKey: String) {
        viewModel.deleteVariable(variableId)
            .subscribe {
                showSnackbar(String.format(getString(R.string.variable_deleted), variableKey))
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
