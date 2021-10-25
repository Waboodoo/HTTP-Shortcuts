package ch.rmy.android.http_shortcuts.activities.editor.body

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.KeyValueDialog
import ch.rmy.android.http_shortcuts.extensions.applyTheme
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.DragOrderingHelper
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class RequestBodyActivity : BaseActivity() {

    private val viewModel: RequestBodyViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val parameters by lazy {
        viewModel.parameters
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private val requestBodyTypeSpinner: LabelledSpinner by bindView(R.id.input_request_body_type)
    private val parameterList: RecyclerView by bindView(R.id.parameter_list)
    private val contentTypeContainer: View by bindView(R.id.container_input_content_type)
    private val contentTypeView: AppCompatAutoCompleteTextView by bindView(R.id.input_content_type)
    private val bodyContentContainer: View by bindView(R.id.container_input_body_content)
    private val bodyContentView: VariableEditText by bindView(R.id.input_body_content)
    private val bodyContentVariableButton: VariableButton by bindView(R.id.variable_button_body_content)
    private val addButton: FloatingActionButton by bindView(R.id.button_add_parameter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_body)
        setTitle(R.string.section_request_body)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        requestBodyTypeSpinner.setItemsFromPairs(REQUEST_BODY_TYPES.map {
            it.first to getString(it.second)
        })

        val adapter = destroyer.own(ParameterAdapter(context, parameters, variablePlaceholderProvider))

        val manager = LinearLayoutManager(context)
        parameterList.layoutManager = manager
        parameterList.setHasFixedSize(true)
        parameterList.adapter = adapter

        initDragOrdering()

        adapter.clickListener = { it.value?.let { parameter -> showEditDialog(parameter) } }
        addButton.applyTheme(themeHelper)
        addButton.setOnClickListener {
            if (shortcutData.value?.requestBodyType == Shortcut.REQUEST_BODY_TYPE_FORM_DATA) {
                showParameterTypeDialog()
            } else {
                showAddDialogForStringParameter()
            }
        }

        VariableViewUtils.bindVariableViews(bodyContentView, bodyContentVariableButton, variablePlaceholderProvider)

        contentTypeView.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, CONTENT_TYPE_SUGGESTIONS))
    }

    private fun initDragOrdering() {
        val dragOrderingHelper = DragOrderingHelper { parameters.size > 1 }
        dragOrderingHelper.attachTo(parameterList)
        dragOrderingHelper.positionChangeSource
            .concatMapCompletable { (oldPosition, newPosition) ->
                viewModel.moveParameter(oldPosition, newPosition)
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
        }
        variablesData.observe(this) {
            updateShortcutViews()
        }

        requestBodyTypeSpinner.selectionChanges
            .concatMapCompletable { type -> viewModel.setRequestBodyType(type) }
            .subscribe()
            .attachTo(destroyer)

        bindTextChangeListener(contentTypeView) { shortcutData.value?.contentType }
        bindTextChangeListener(bodyContentView) { shortcutData.value?.bodyContent }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setRequestBody(
            contentType = contentTypeView.text.toString(),
            bodyContent = bodyContentView.rawString
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        requestBodyTypeSpinner.selectedItem = shortcut.requestBodyType
        contentTypeView.setTextSafely(shortcut.contentType)
        bodyContentView.rawString = shortcut.bodyContent

        val usesParameters = shortcut.usesRequestParameters()
        parameterList.visible = usesParameters
        addButton.visible = usesParameters

        val usesCustomBody = shortcut.usesCustomBody()
        contentTypeContainer.visible = usesCustomBody
        bodyContentContainer.visible = usesCustomBody
    }

    private fun showEditDialog(parameter: Parameter) {
        if (parameter.isFileParameter || parameter.isFilesParameter) {
            showEditDialogForFileParameter(parameter)
        } else {
            showEditDialogForStringParameter(parameter)
        }
    }

    private fun showEditDialogForStringParameter(parameter: Parameter) {
        val parameterId = parameter.id
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_edit),
            keyLabel = getString(R.string.label_post_param_key),
            valueLabel = getString(R.string.label_post_param_value),
            data = parameter.key to parameter.value,
            isMultiLine = true,
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> {
                        viewModel.updateParameter(parameterId, event.data.first, value = event.data.second)
                    }
                    is KeyValueDialog.Event.DataRemovedEvent -> {
                        viewModel.removeParameter(parameterId)
                    }
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showEditDialogForFileParameter(parameter: Parameter) {
        val parameterId = parameter.id
        FileParameterDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_edit_file),
            showRemoveOption = true,
            showFileNameOption = parameter.isFileParameter,
            keyName = parameter.key,
            fileName = parameter.fileName
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is FileParameterDialog.Event.DataChangedEvent -> {
                        viewModel.updateParameter(parameterId, event.keyName, fileName = event.fileName)
                    }
                    is FileParameterDialog.Event.DataRemovedEvent -> {
                        viewModel.removeParameter(parameterId)
                    }
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showParameterTypeDialog() {
        DialogBuilder(context)
            .title(R.string.dialog_title_parameter_type)
            .item(R.string.option_parameter_type_string) {
                showAddDialogForStringParameter()
            }
            .item(R.string.option_parameter_type_file) {
                showAddDialogForFileParameter(multiple = false)
            }
            .item(R.string.option_parameter_type_files) {
                showAddDialogForFileParameter(multiple = true)
            }
            .showIfPossible()
    }

    private fun showAddDialogForStringParameter() {
        KeyValueDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_add),
            keyLabel = getString(R.string.label_post_param_key),
            valueLabel = getString(R.string.label_post_param_value),
            isMultiLine = true
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is KeyValueDialog.Event.DataChangedEvent -> viewModel.addStringParameter(event.data.first, event.data.second)
                    else -> Completable.complete()
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun showAddDialogForFileParameter(multiple: Boolean) {
        FileParameterDialog(
            variablePlaceholderProvider = variablePlaceholderProvider,
            title = getString(R.string.title_post_param_add_file),
            showFileNameOption = !multiple
        )
            .show(context)
            .flatMapCompletable { event ->
                when (event) {
                    is FileParameterDialog.Event.DataChangedEvent -> viewModel.addFileParameter(event.keyName, event.fileName, multiple)
                    else -> Completable.complete()
                }
            }
            .subscribe()
            .attachTo(destroyer)
    }

    override fun onBackPressed() {
        updateViewModelFromViews()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, RequestBodyActivity::class.java)

    companion object {

        private val REQUEST_BODY_TYPES = listOf(
            Shortcut.REQUEST_BODY_TYPE_FORM_DATA to R.string.request_body_option_form_data,
            Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE to R.string.request_body_option_x_www_form_urlencoded,
            Shortcut.REQUEST_BODY_TYPE_CUSTOM_TEXT to R.string.request_body_option_custom_text,
            Shortcut.REQUEST_BODY_TYPE_FILE to R.string.request_body_option_file
        )

        private val CONTENT_TYPE_SUGGESTIONS = listOf(
            "application/javascript",
            "application/json",
            "application/octet-stream",
            "application/xml",
            "text/css",
            "text/csv",
            "text/plain",
            "text/html",
            "text/xml"
        )

    }

}