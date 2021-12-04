package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.lifecycle.LiveData
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.CodeSnippetPicker
import ch.rmy.android.http_shortcuts.data.models.Base
import ch.rmy.android.http_shortcuts.databinding.ActivityGlobalScriptingBinding
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.insertAroundCursor
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.openURL
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class GlobalScriptingActivity : BaseActivity() {

    private val viewModel: GlobalScriptingViewModel by bindViewModel()

    private val baseData: LiveData<Base?>
        get() = viewModel.base

    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }
    private val shortcutsData by lazy {
        viewModel.shortcuts
    }
    private val shortcutPlaceholderProvider by lazy {
        ShortcutPlaceholderProvider(shortcutsData)
    }
    private val iconPicker: IconPicker by lazy {
        IconPicker(this) { iconName ->
            Completable.fromAction {
                codeSnippetPicker.insertChangeIconSnippet(
                    viewModel.iconPickerShortcutPlaceholder ?: return@fromAction,
                    getCodeInsertion(),
                    iconName,
                )
            }
        }
    }
    private val codeSnippetPicker by lazy {
        CodeSnippetPicker(
            context,
            null,
            variablePlaceholderProvider,
            shortcutPlaceholderProvider,
        ) { shortcutPlaceholder ->
            viewModel.iconPickerShortcutPlaceholder = shortcutPlaceholder
            iconPicker.openIconSelectionDialog()
        }
    }
    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }
    private val shortcutPlaceholderColor by lazy {
        color(context, R.color.shortcut)
    }

    private lateinit var binding: ActivityGlobalScriptingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityGlobalScriptingBinding.inflate(layoutInflater))

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.buttonAddCodeSnippet.setOnClickListener {
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(), includeResponseOptions = false)
        }
    }

    private fun getCodeInsertion(): (String, String) -> Unit =
        { before, after ->
            binding.inputCode.insertAroundCursor(before, after)
            Variables.applyVariableFormattingToJS(binding.inputCode.text!!, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(binding.inputCode.text!!, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }

    private fun bindViewsToViewModel() {
        baseData.observe(this) {
            val base = viewModel.base.value ?: return@observe
            initViews(base)
            baseData.removeObservers(this)
        }
        viewModel.hasChanges.observe(this) {
            invalidateOptionsMenu()
        }
    }

    private fun initViews(base: Base) {
        binding.inputCode.setTextSafely(processTextForView(base.globalCode ?: ""))
        bindTextChangeListener(binding.inputCode)
    }

    private fun processTextForView(input: String): CharSequence {
        val text = SpannableStringBuilder(input)
        Variables.applyVariableFormattingToJS(
            text,
            variablePlaceholderProvider,
            variablePlaceholderColor,
        )
        ShortcutSpanManager.applyShortcutFormattingToJS(
            text,
            shortcutPlaceholderProvider,
            shortcutPlaceholderColor,
        )
        return text
    }

    private fun bindTextChangeListener(textView: EditText) {
        textView.observeTextChanges()
            .throttleFirst(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.hasChanges.value = true
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.global_scripting_activity_menu, menu)
        menu.findItem(R.id.action_save_changes).isVisible = viewModel.hasChanges.value ?: false
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { showHelp() }
        R.id.action_save_changes -> consume { save() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showHelp() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    private fun save() {
        viewModel.setCode(binding.inputCode.text.toString())
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    override fun onBackPressed() {
        if (viewModel.hasChanges.value == true) {
            DialogBuilder(context)
                .message(R.string.confirm_discard_changes_message)
                .positive(R.string.dialog_discard) { finish() }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
        } else {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            codeSnippetPicker.handleRequestResult(
                getCodeInsertion(),
                requestCode,
                data,
            )
        }
        iconPicker.handleResult(requestCode, resultCode, data)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, GlobalScriptingActivity::class.java)
}
