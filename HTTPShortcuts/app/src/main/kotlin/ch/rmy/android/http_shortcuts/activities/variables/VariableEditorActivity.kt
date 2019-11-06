package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.VariableEditorFragment
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import kotterknife.bindView

class VariableEditorActivity : BaseActivity() {

    private val viewModel: VariableEditorViewModel by bindViewModel()

    private val variable by lazy {
        viewModel.getVariable()
    }

    // Views
    private val typeSpinner: LabelledSpinner by bindView(R.id.input_variable_type)
    private val keyView: EditText by bindView(R.id.input_variable_key)
    private val titleView: EditText by bindView(R.id.input_variable_title)
    private val titleViewContainer: View by bindView(R.id.dialog_title_container)
    private val urlEncode: CheckBox by bindView(R.id.input_url_encode)
    private val jsonEncode: CheckBox by bindView(R.id.input_json_encode)
    private val allowShare: CheckBox by bindView(R.id.input_allow_share)

    private var fragment: VariableEditorFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        initViews()
    }

    private fun initViewModel() {
        viewModel.variableId = intent.getStringExtra(EXTRA_VARIABLE_ID)
    }

    private fun initViews() {
        setContentView(R.layout.activity_variable_editor)

        keyView.setText(variable.key)
        titleView.setText(variable.title)
        val defaultColor = keyView.textColors
        keyView
            .observeTextChanges()
            .subscribe { text ->
                if (text.isEmpty() || Variables.isValidVariableKey(text.toString())) {
                    keyView.setTextColor(defaultColor)
                    keyView.error = null
                } else {
                    keyView.setTextColor(Color.RED)
                    keyView.error = getString(R.string.warning_invalid_variable_key)
                }
            }
            .attachTo(destroyer)

        typeSpinner.setItemsFromPairs(VARIABLE_TYPES.map {
            it.first to getString(it.second)
        })
        typeSpinner.selectedItem = variable.type
        typeSpinner.selectionChanges
            .subscribe {
                updateTypeEditor()
            }
            .attachTo(destroyer)

        urlEncode.isChecked = variable.urlEncode
        jsonEncode.isChecked = variable.jsonEncode
        allowShare.isChecked = variable.isShareText

        setTitle(if (variable.isNew) R.string.create_variable else R.string.edit_variable)

        updateTypeEditor()
    }

    private fun updateTypeEditor() {
        compileVariable()
        val variableType = VariableTypeFactory.getType(typeSpinner.selectedItem)
        val fragmentManager = supportFragmentManager
        fragment = variableType.getEditorFragment(fragmentManager)

        titleViewContainer.visible = (variableType as? AsyncVariableType)?.hasTitle == true

        fragment?.let { fragment ->
            fragmentManager
                .beginTransaction()
                .replace(R.id.variable_type_fragment_container, fragment, variableType.tag)
                .commitAllowingStateLoss()
        }
    }

    fun onFragmentStarted() {
        fragment?.updateViews(variable)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.variable_editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { confirmClose() }
        R.id.action_save_variable -> consume { trySave() }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        confirmClose()
    }

    private fun confirmClose() {
        compileVariable()
        if (viewModel.hasChanges()) {
            DialogBuilder(context)
                .message(R.string.confirm_discard_changes_message)
                .positive(R.string.dialog_discard) { finish() }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
        } else {
            finish()
        }
    }

    private fun trySave() {
        compileVariable()
        if (validate()) {
            viewModel.save()
                .subscribe {
                    finish()
                }
                .attachTo(destroyer)
        }
    }

    private fun compileVariable() {
        fragment?.compileIntoVariable(variable)
        variable.title = titleView.text.toString().trim { it <= ' ' }
        variable.key = keyView.text.toString()
        variable.type = typeSpinner.selectedItem
        variable.urlEncode = urlEncode.isChecked
        variable.jsonEncode = jsonEncode.isChecked
        variable.isShareText = allowShare.isChecked
    }

    private fun validate(): Boolean {
        if (variable.key.isEmpty()) {
            keyView.error = getString(R.string.validation_key_non_empty)
            keyView.focus()
            return false
        }
        if (viewModel.isKeyAlreadyInUsed()) {
            keyView.error = getString(R.string.validation_key_already_exists)
            keyView.focus()
            return false
        }
        return fragment == null || fragment!!.validate()
    }

    override fun onStop() {
        super.onStop()
        compileVariable()
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, VariableEditorActivity::class.java) {

        fun variableId(variableId: String) = also {
            intent.putExtra(EXTRA_VARIABLE_ID, variableId)
        }

    }

    companion object {

        private const val EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity.variable_id"

        val VARIABLE_TYPES = listOf(
            Variable.TYPE_CONSTANT to R.string.variable_type_constant,
            Variable.TYPE_TEXT to R.string.variable_type_text,
            Variable.TYPE_NUMBER to R.string.variable_type_number,
            Variable.TYPE_PASSWORD to R.string.variable_type_password,
            Variable.TYPE_DATE to R.string.variable_type_date,
            Variable.TYPE_TIME to R.string.variable_type_time,
            Variable.TYPE_COLOR to R.string.variable_type_color,
            Variable.TYPE_SELECT to R.string.variable_type_select,
            Variable.TYPE_TOGGLE to R.string.variable_type_toggle,
            Variable.TYPE_SLIDER to R.string.variable_type_slider
        )

    }

}
