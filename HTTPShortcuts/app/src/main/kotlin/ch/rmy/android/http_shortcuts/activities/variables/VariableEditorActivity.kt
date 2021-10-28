package ch.rmy.android.http_shortcuts.activities.variables

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.ActivityVariableEditorBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.variables.types.HasTitle
import ch.rmy.android.http_shortcuts.variables.types.VariableEditorFragment
import ch.rmy.android.http_shortcuts.variables.types.VariableTypeFactory

class VariableEditorActivity : BaseActivity() {

    private val variableId: String? by lazy {
        intent.getStringExtra(EXTRA_VARIABLE_ID)
    }
    private val preferredType: String? by lazy {
        intent.getStringExtra(EXTRA_VARIABLE_TYPE)
    }

    private val viewModel: VariableEditorViewModel by bindViewModel()

    private val variable by lazy {
        viewModel.getVariable()
    }

    private lateinit var binding: ActivityVariableEditorBinding

    private var fragment: VariableEditorFragment<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityVariableEditorBinding.inflate(layoutInflater))
        initViewModel()
        initViews()
    }

    private fun initViewModel() {
        viewModel.variableType = preferredType ?: Variable.TYPE_CONSTANT
        viewModel.variableId = variableId
    }

    private fun initViews() {
        setSubtitle(VariableTypes.getTypeName(variable.type))
        binding.inputVariableKey.setText(variable.key)
        binding.inputVariableTitle.setText(variable.title)
        val defaultColor = binding.inputVariableKey.textColors
        binding.inputVariableKey
            .observeTextChanges()
            .subscribe { text ->
                if (text.isEmpty() || Variables.isValidVariableKey(text.toString())) {
                    binding.inputVariableKey.setTextColor(defaultColor)
                    binding.inputVariableKey.error = null
                } else {
                    binding.inputVariableKey.setTextColor(Color.RED)
                    binding.inputVariableKey.error = getString(R.string.warning_invalid_variable_key)
                }
            }
            .attachTo(destroyer)

        binding.inputUrlEncode.isChecked = variable.urlEncode
        binding.inputJsonEncode.isChecked = variable.jsonEncode
        binding.inputAllowShare.isChecked = variable.isShareText

        setTitle(if (variable.isNew) R.string.create_variable else R.string.edit_variable)

        updateTypeEditor()
    }

    private fun updateTypeEditor() {
        compileVariable()
        val variableType = VariableTypeFactory.getType(variable.type)

        fragment = variableType.getEditorFragment(supportFragmentManager)

        binding.dialogTitleContainer.visible = variableType is HasTitle

        fragment?.let { fragment ->
            supportFragmentManager
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
        variable.title = binding.inputVariableTitle.text.toString().trim { it <= ' ' }
        variable.key = binding.inputVariableKey.text.toString()
        variable.urlEncode = binding.inputUrlEncode.isChecked
        variable.jsonEncode = binding.inputJsonEncode.isChecked
        variable.isShareText = binding.inputAllowShare.isChecked
    }

    private fun validate(): Boolean {
        if (variable.key.isEmpty()) {
            binding.inputVariableKey.error = getString(R.string.validation_key_non_empty)
            binding.inputVariableKey.focus()
            return false
        }
        if (viewModel.isKeyAlreadyInUsed()) {
            binding.inputVariableKey.error = getString(R.string.validation_key_already_exists)
            binding.inputVariableKey.focus()
            return false
        }
        return fragment == null || fragment!!.validate()
    }

    override fun onStop() {
        super.onStop()
        compileVariable()
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, VariableEditorActivity::class.java) {

        fun variableType(type: String) = also {
            intent.putExtra(EXTRA_VARIABLE_TYPE, type)
        }

        fun variableId(variableId: String) = also {
            intent.putExtra(EXTRA_VARIABLE_ID, variableId)
        }

    }

    companion object {

        private const val EXTRA_VARIABLE_ID = "ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity.variable_id"
        private const val EXTRA_VARIABLE_TYPE = "ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity.variable_type"

    }

}
