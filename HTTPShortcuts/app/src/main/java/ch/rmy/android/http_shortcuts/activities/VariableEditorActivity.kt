package ch.rmy.android.http_shortcuts.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.detachFromRealm
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.ArrayUtil
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.fix
import ch.rmy.android.http_shortcuts.utils.focus
import ch.rmy.android.http_shortcuts.utils.onTextChanged
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.visible
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.variables.types.AsyncVariableType
import ch.rmy.android.http_shortcuts.variables.types.VariableEditorFragment
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner
import kotterknife.bindView

class VariableEditorActivity : BaseActivity() {

    private val typeSpinner: LabelledSpinner by bindView(R.id.input_variable_type)
    private val keyView: EditText by bindView(R.id.input_variable_key)
    private val titleView: EditText by bindView(R.id.input_variable_title)
    private val urlEncode: CheckBox by bindView(R.id.input_url_encode)
    private val jsonEncode: CheckBox by bindView(R.id.input_json_encode)
    private val allowShare: CheckBox by bindView(R.id.input_allow_share)

    private val controller by lazy { destroyer.own(Controller()) }
    private lateinit var variable: Variable
    private lateinit var oldVariable: Variable

    private var fragment: VariableEditorFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_variable_editor)

        val variableId = intent.getLongExtra(EXTRA_VARIABLE_ID, 0)
        val variable = if (savedInstanceState?.containsKey(STATE_JSON_VARIABLE) == true) {
            GsonUtil.fromJson(savedInstanceState.getString(STATE_JSON_VARIABLE)!!, Variable::class.java)
        } else {
            if (variableId == 0L) Variable.createNew() else controller.getVariableById(variableId)?.detachFromRealm()
        }
        if (variable == null) {
            finish()
            return
        }
        this.variable = variable
        oldVariable = (if (variableId != 0L) controller.getVariableById(variableId)?.detachFromRealm() else null) ?: Variable.createNew()

        initViews()
        initTypeSelector()
    }

    private fun initViews() {
        keyView.setText(variable.key)
        titleView.setText(variable.title)
        val defaultColor = keyView.textColors
        keyView.onTextChanged { text ->
            if (text.isEmpty() || Variables.isValidVariableKey(text.toString())) {
                keyView.setTextColor(defaultColor)
                keyView.error = null
            } else {
                keyView.setTextColor(Color.RED)
                keyView.error = getString(R.string.warning_invalid_variable_key)
            }
        }.attachTo(destroyer)

        typeSpinner.setItemsArray(ShortcutUIUtils.getVariableTypeOptions(context))
        typeSpinner.fix()
        typeSpinner.setSelection(ArrayUtil.findIndex(Variable.TYPE_OPTIONS, variable.type))

        urlEncode.isChecked = variable.urlEncode
        jsonEncode.isChecked = variable.jsonEncode
        allowShare.isChecked = variable.isShareText

        setTitle(if (variable.isNew) R.string.create_variable else R.string.edit_variable)

        updateTypeEditor()
    }

    private fun initTypeSelector() {
        typeSpinner.onItemChosenListener = object : OnItemChosenListener() {
            override fun onSelectionChanged() {
                updateTypeEditor()
            }
        }
    }

    private fun updateTypeEditor() {
        compileVariable()
        val variableType = VariableTypeFactory.getType(selectedType)
        val fragmentManager = supportFragmentManager
        fragment = variableType.getEditorFragment(fragmentManager)

        titleView.visible = (variableType as? AsyncVariableType)?.hasTitle == true

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

    private val selectedType: String
        get() = Variable.TYPE_OPTIONS[typeSpinner.spinner.selectedItemPosition]

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
        if (hasChanges()) {
            MaterialDialog.Builder(context)
                    .content(R.string.confirm_discard_changes_message)
                    .positiveText(R.string.dialog_discard)
                    .onPositive { _, _ -> finish() }
                    .negativeText(R.string.dialog_cancel)
                    .showIfPossible()
        } else {
            finish()
        }
    }

    private fun hasChanges() = !oldVariable.isSameAs(variable)

    private fun trySave() {
        compileVariable()
        if (validate()) {
            controller.persist(variable)
                    .done {
                        finish()
                    }
        }
    }

    private fun compileVariable() {
        fragment?.compileIntoVariable(variable)
        variable.title = titleView.text.toString().trim { it <= ' ' }
        variable.key = keyView.text.toString()
        variable.type = Variable.TYPE_OPTIONS[typeSpinner.spinner.selectedItemPosition]
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
        val otherVariable = controller.getVariableByKey(variable.key)
        if (otherVariable != null && otherVariable.id != variable.id) {
            keyView.error = getString(R.string.validation_key_already_exists)
            keyView.focus()
            return false
        }
        return fragment == null || fragment!!.validate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        compileVariable()
        outState.putString(STATE_JSON_VARIABLE, GsonUtil.toJson(variable))
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, VariableEditorActivity::class.java) {

        fun variableId(variableId: Long) = this.also {
            intent.putExtra(EXTRA_VARIABLE_ID, variableId)
        }

    }

    companion object {

        private const val EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.VariableEditorActivity.variable_id"
        private const val STATE_JSON_VARIABLE = "variable_json"

    }

}
