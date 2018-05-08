package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionsView
import ch.rmy.android.http_shortcuts.dialogs.IconNameChangeDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.icons.IconSelector
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValueList
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.detachFromRealm
import ch.rmy.android.http_shortcuts.realm.models.Header
import ch.rmy.android.http_shortcuts.realm.models.Parameter
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.utils.ArrayUtil
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.utils.applyToShortcut
import ch.rmy.android.http_shortcuts.utils.consume
import ch.rmy.android.http_shortcuts.utils.dimen
import ch.rmy.android.http_shortcuts.utils.fix
import ch.rmy.android.http_shortcuts.utils.focus
import ch.rmy.android.http_shortcuts.utils.logException
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.utils.visible
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.curlcommand.CurlCommand
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner
import com.theartofdev.edmodo.cropper.CropImage
import kotterknife.bindView


@SuppressLint("InflateParams")
class EditorActivity : BaseActivity() {

    private var shortcutId: Long = 0

    private val controller by lazy { destroyer.own(Controller()) }
    private val variableKeyProvider by lazy {
        destroyer.own(VariablePlaceholderProvider(context, controller.getVariables()))
    }

    private lateinit var oldShortcut: Shortcut
    private lateinit var shortcut: Shortcut

    private val methodView: LabelledSpinner by bindView(R.id.input_method)
    private val feedbackView: LabelledSpinner by bindView(R.id.input_feedback)
    private val timeoutView: LabelledSpinner by bindView(R.id.input_timeout)
    private val retryPolicyView: LabelledSpinner by bindView(R.id.input_retry_policy)
    private val nameView: EditText by bindView(R.id.input_shortcut_name)
    private val descriptionView: EditText by bindView(R.id.input_description)
    private val urlView: VariableEditText by bindView(R.id.input_url)
    private val authenticationView: LabelledSpinner by bindView(R.id.input_authentication)
    private val usernameView: VariableEditText by bindView(R.id.input_username)
    private val passwordView: VariableEditText by bindView(R.id.input_password)
    private val iconView: IconView by bindView(R.id.input_icon)
    private val iconViewContainer: View by bindView(R.id.icon_container)
    private val parameterList: KeyValueList<Parameter> by bindView(R.id.post_parameter_list)
    private val customHeaderList: KeyValueList<Header> by bindView(R.id.custom_headers_list)
    private val requestBodyView: VariableEditText by bindView(R.id.input_custom_body)
    private val acceptCertificatesCheckbox: CheckBox by bindView(R.id.input_accept_all_certificates)
    private val authenticationContainer: LinearLayout by bindView(R.id.authentication_container)
    private val requestBodyContainer: LinearLayout by bindView(R.id.section_request_body)
    private val launcherShortcutCheckbox: CheckBox by bindView(R.id.input_launcher_shortcut)
    private val delayView: LabelledSpinner by bindView(R.id.input_delay_execution)
    private val variableButtonUrl: VariableButton by bindView(R.id.variable_button_url)
    private val variableButtonUsername: VariableButton by bindView(R.id.variable_button_username)
    private val variableButtonPassword: VariableButton by bindView(R.id.variable_button_password)
    private val variableButtonRequestBody: VariableButton by bindView(R.id.variable_button_custom_body)
    private val requestBodyTypeView: LabelledSpinner by bindView(R.id.input_request_body_type)
    private val requestParametersContainer: View by bindView(R.id.request_parameters_container)
    private val requestCustomBodyContainer: View by bindView(R.id.request_body_custom_content_type_container)
    private val customContentType: AutoCompleteTextView by bindView(R.id.input_content_type)
    private val beforeActionsView: ActionsView by bindView(R.id.before_actions)
    private val successActionsView: ActionsView by bindView(R.id.success_actions)
    private val failureActionsView: ActionsView by bindView(R.id.failure_actions)

    private val itemChosenListener = object : OnItemChosenListener() {
        override fun onSelectionChanged() {
            compileShortcut()
            updateUI()
        }
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        shortcutId = intent.getLongExtra(EXTRA_SHORTCUT_ID, 0)
        val shortcut = if (savedInstanceState?.containsKey(STATE_JSON_SHORTCUT) == true) {
            GsonUtil.fromJson(savedInstanceState.getString(STATE_JSON_SHORTCUT)!!, Shortcut::class.java)
        } else {
            if (shortcutId == 0L) Shortcut.createNew() else controller.getShortcutById(shortcutId)?.detachFromRealm()
        }
        if (shortcut == null) {
            finish()
            return
        }
        this.shortcut = shortcut
        oldShortcut = (if (shortcutId != 0L) controller.getShortcutById(shortcutId)?.detachFromRealm() else null) ?: Shortcut.createNew()

        if (shortcut.isNew) {
            val curlCommand = (intent.getSerializableExtra(EXTRA_CURL_COMMAND) as? CurlCommand)
            curlCommand?.applyToShortcut(shortcut)

            if (shortcut.iconName == null) {
                shortcut.iconName = Icons.getRandomIcon(context)
                oldShortcut.iconName = shortcut.iconName
            } else if (savedInstanceState != null && savedInstanceState.containsKey(STATE_INITIAL_ICON)) {
                oldShortcut.iconName = savedInstanceState.getString(STATE_INITIAL_ICON)
            }
        }

        initViews()
    }

    private fun initViews() {
        initVariableEditText(urlView, variableButtonUrl, shortcut.url)
        initVariableEditText(usernameView, variableButtonUsername, shortcut.username)
        initVariableEditText(passwordView, variableButtonPassword, shortcut.password)
        initVariableEditText(requestBodyView, variableButtonRequestBody, shortcut.bodyContent)

        nameView.setText(shortcut.name)
        descriptionView.setText(shortcut.description)

        methodView.setItemsArray(Shortcut.METHODS)
        methodView.fix()
        methodView.onItemChosenListener = itemChosenListener
        methodView.setSelection(ArrayUtil.findIndex(Shortcut.METHODS, shortcut.method))

        authenticationView.setItemsArray(ShortcutUIUtils.getAuthenticationOptions(context))
        authenticationView.fix()
        authenticationView.onItemChosenListener = itemChosenListener
        authenticationView.setSelection(ArrayUtil.findIndex(Shortcut.AUTHENTICATION_OPTIONS, shortcut.authentication!!))

        parameterList.variablePlaceholderProvider = variableKeyProvider
        parameterList.addItems(shortcut.parameters)
        parameterList.setButtonText(R.string.button_add_post_param)
        parameterList.addDialogTitle = R.string.title_post_param_add
        parameterList.editDialogTitle = R.string.title_post_param_edit
        parameterList.keyLabel = R.string.label_post_param_key
        parameterList.valueLabel = R.string.label_post_param_value
        parameterList.isMultiLine = true
        parameterList.factory = { key, value -> Parameter.createNew(key, value) }

        customHeaderList.variablePlaceholderProvider = variableKeyProvider
        customHeaderList.addItems(shortcut.headers)
        customHeaderList.setButtonText(R.string.button_add_custom_header)
        customHeaderList.addDialogTitle = R.string.title_custom_header_add
        customHeaderList.editDialogTitle = R.string.title_custom_header_edit
        customHeaderList.keyLabel = R.string.label_custom_header_key
        customHeaderList.valueLabel = R.string.label_custom_header_value
        customHeaderList.factory = { key, value -> Header.createNew(key, value) }
        customHeaderList.setSuggestions(Header.SUGGESTED_KEYS)

        feedbackView.setItemsArray(ShortcutUIUtils.getFeedbackOptions(context))
        feedbackView.onItemChosenListener = itemChosenListener
        feedbackView.fix()
        feedbackView.setSelection(ArrayUtil.findIndex(Shortcut.FEEDBACK_OPTIONS, shortcut.feedback))

        beforeActionsView.isBeforeActions = true
        beforeActionsView.variablePlaceholderProvider = variableKeyProvider
        beforeActionsView.attachTo(destroyer)
        beforeActionsView.actions = shortcut.beforeActions

        successActionsView.variablePlaceholderProvider = variableKeyProvider
        successActionsView.attachTo(destroyer)
        successActionsView.actions = shortcut.successActions

        failureActionsView.variablePlaceholderProvider = variableKeyProvider
        failureActionsView.attachTo(destroyer)
        failureActionsView.actions = shortcut.failureActions

        timeoutView.setItemsArray(ShortcutUIUtils.getTimeoutOptions(context))
        timeoutView.fix()
        timeoutView.setSelection(ArrayUtil.findIndex(Shortcut.TIMEOUT_OPTIONS, shortcut.timeout))

        retryPolicyView.setItemsArray(ShortcutUIUtils.getRetryPolicyOptions(context))
        retryPolicyView.fix()
        retryPolicyView.setSelection(ArrayUtil.findIndex(Shortcut.RETRY_POLICY_OPTIONS, shortcut.retryPolicy))

        acceptCertificatesCheckbox.isChecked = shortcut.acceptAllCertificates
        launcherShortcutCheckbox.isChecked = shortcut.launcherShortcut

        delayView.setItemsArray(ShortcutUIUtils.getDelayOptions(context))
        delayView.fix()
        delayView.setSelection(ArrayUtil.findIndex(Shortcut.DELAY_OPTIONS, shortcut.delay))

        requestBodyTypeView.setItemsArray(ShortcutUIUtils.getRequestBodyTypeOptions(context))
        requestBodyTypeView.fix()
        requestBodyTypeView.onItemChosenListener = itemChosenListener
        requestBodyTypeView.setSelection(ArrayUtil.findIndex(Shortcut.REQUEST_BODY_TYPE_OPTIONS, shortcut.requestBodyType))

        iconViewContainer.setOnClickListener { openIconSelectionDialog() }

        customContentType.setText(shortcut.contentType)
        customContentType.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, Shortcut.CONTENT_TYPE_SUGGESTIONS))

        setTitle(if (shortcut.isNew) R.string.create_shortcut else R.string.edit_shortcut)
        updateUI()
    }

    private fun initVariableEditText(editText: VariableEditText, variableButton: VariableButton, value: String) {
        editText.bind(variableButton, variableKeyProvider).attachTo(destroyer)
        editText.rawString = value
    }

    private fun updateUI() {
        iconView.setImageURI(shortcut.getIconURI(context), shortcut.iconName)
        retryPolicyView.visible = shortcut.isRetryAllowed()
        requestBodyContainer.visible = shortcut.allowsBody()
        authenticationContainer.visible = shortcut.usesAuthentication()
        launcherShortcutCheckbox.visible = LauncherShortcutManager.supportsLauncherShortcuts()
        requestParametersContainer.visible = shortcut.usesRequestParameters()
        requestCustomBodyContainer.visible = shortcut.usesCustomBody()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { confirmClose() }
        R.id.action_save_shortcut -> consume { trySave() }
        R.id.action_test_shortcut -> consume { test() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun trySave() {
        compileShortcut()
        if (validate(false)) {
            shortcut.id = shortcutId
            controller.persist(shortcut)
                    .done { persistedShortcut ->
                        onShortcutSaved(persistedShortcut)
                    }
        }
    }

    private fun validate(testOnly: Boolean) =
            if (!testOnly && shortcut.name.isBlank()) {
                nameView.error = getString(R.string.validation_name_not_empty)
                nameView.focus()
                false
            } else if (!Validation.isAcceptableUrl(shortcut.url)) {
                urlView.error = getString(R.string.validation_url_invalid)
                urlView.focus()
                false
            } else {
                true
            }

    private fun nameOrIconChanged() =
            oldShortcut.name != shortcut.name || oldShortcut.iconName != shortcut.iconName

    private fun onShortcutSaved(persistedShortcut: Shortcut) {
        val returnIntent = Intent()
        returnIntent.putExtra(EXTRA_SHORTCUT_ID, persistedShortcut.id)
        setResult(Activity.RESULT_OK, returnIntent)
        val dialog = IconNameChangeDialog(context)
        if (LauncherShortcutManager.supportsPinning(context)) {
            LauncherShortcutManager.updatePinnedShortcut(context, persistedShortcut)
            finish()
        } else if (!oldShortcut.isNew && nameOrIconChanged() && dialog.shouldShow()) {
            dialog.show(MaterialDialog.SingleButtonCallback { _, _ -> finish() })
        } else {
            finish()
        }
    }

    private fun test() {
        compileShortcut()
        if (validate(true)) {
            shortcut.id = TEMPORARY_ID
            controller.persist(shortcut)
                    .done {
                        val intent = ExecuteActivity.IntentBuilder(context, TEMPORARY_ID)
                                .build()
                        startActivity(intent)
                    }
        }
    }

    private fun openIconSelectionDialog() {
        MenuDialogBuilder(context)
                .title(R.string.change_icon)
                .item(R.string.choose_icon, this::openBuiltInIconSelectionDialog)
                .item(R.string.choose_image, this::openImagePicker)
                .item(R.string.choose_ipack_icon, this::openIpackPicker)
                .showIfPossible()
    }

    private fun openBuiltInIconSelectionDialog() {
        IconSelector(context) { iconName ->
            shortcut.iconName = iconName
            updateUI()
        }
                .show()
    }

    private fun openImagePicker() {
        CropImage.activity()
                .setCropMenuCropButtonIcon(R.drawable.ic_save)
                .setCropMenuCropButtonTitle(getString(R.string.button_apply_icon))
                .setAspectRatio(1, 1)
                .setRequestedSize(iconSize, iconSize)
                .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                .setMultiTouchEnabled(true)
                .start(this)
    }

    private fun openIpackPicker() {
        val intent = IpackUtil.getIpackIntent(context)
        startActivityForResult(intent, REQUEST_SELECT_IPACK_ICON)
    }

    override fun onBackPressed() {
        confirmClose()
    }

    private fun confirmClose() {
        compileShortcut()
        if (hasChanges()) {
            MaterialDialog.Builder(context)
                    .content(R.string.confirm_discard_changes_message)
                    .positiveText(R.string.dialog_discard)
                    .onPositive { _, _ -> cancelAndClose() }
                    .negativeText(R.string.dialog_cancel)
                    .showIfPossible()
        } else {
            cancelAndClose()
        }
    }

    private fun hasChanges() = !oldShortcut.isSameAs(shortcut)

    private fun compileShortcut() {
        shortcut.apply {
            name = nameView.text.toString().trim { it <= ' ' }
            url = urlView.rawString
            method = Shortcut.METHODS[methodView.spinner.selectedItemPosition]
            description = descriptionView.text.toString().trim { it <= ' ' }
            password = passwordView.rawString
            username = usernameView.rawString
            bodyContent = requestBodyView.rawString
            feedback = Shortcut.FEEDBACK_OPTIONS[feedbackView.spinner.selectedItemPosition]
            timeout = Shortcut.TIMEOUT_OPTIONS[timeoutView.spinner.selectedItemPosition]
            delay = Shortcut.DELAY_OPTIONS[delayView.spinner.selectedItemPosition]
            shortcut.authentication = Shortcut.AUTHENTICATION_OPTIONS[authenticationView.spinner.selectedItemPosition]
            retryPolicy = Shortcut.RETRY_POLICY_OPTIONS[retryPolicyView.spinner.selectedItemPosition]
            requestBodyType = Shortcut.REQUEST_BODY_TYPE_OPTIONS[requestBodyTypeView.spinner.selectedItemPosition]
            acceptAllCertificates = acceptCertificatesCheckbox.isChecked
            launcherShortcut = launcherShortcutCheckbox.isChecked
            contentType = customContentType.text.toString().trim { it <= ' ' }
            parameters.clear()
            parameters.addAll(parameterList.items)
            headers.clear()
            headers.addAll(customHeaderList.items)
            beforeActions = beforeActionsView.actions
            successActions = successActionsView.actions
            failureActions = failureActionsView.actions
        }
    }

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                try {
                    val result = CropImage.getActivityResult(intent)
                    if (resultCode == RESULT_OK) {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)
                        val iconName = UUIDUtils.create() + ".png"
                        openFileOutput(iconName, 0).use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            it.flush()
                        }
                        bitmap.recycle()
                        shortcut.iconName = iconName
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        if (result.error != null) {
                            logException(result.error)
                        }
                        showSnackbar(getString(R.string.error_set_image))
                    }
                } catch (e: Exception) {
                    logException(e)
                    showSnackbar(getString(R.string.error_set_image))
                }
            }
            REQUEST_SELECT_IPACK_ICON -> {
                if (resultCode == RESULT_OK && intent != null) {
                    shortcut.iconName = IpackUtil.getIpackUri(intent).toString()
                }
            }
        }
        updateUI()
    }

    private val iconSize by lazy {
        Math.max(dimen(android.R.dimen.app_icon_size), launcherLargeIconSize)
    }

    private val launcherLargeIconSize: Int
        get() = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconSize

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        compileShortcut()
        outState.putString(STATE_JSON_SHORTCUT, GsonUtil.toJson(shortcut))
        outState.putString(STATE_INITIAL_ICON, oldShortcut.iconName)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, EditorActivity::class.java) {

        fun shortcutId(shortcutId: Long) = this.also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun curlCommand(command: CurlCommand) = this.also {
            intent.putExtra(EXTRA_CURL_COMMAND, command)
        }

    }

    companion object {

        const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.EditorActivity.shortcut_id"
        private const val EXTRA_CURL_COMMAND = "ch.rmy.android.http_shortcuts.activities.EditorActivity.curl_command"

        private const val REQUEST_SELECT_IPACK_ICON = 3
        private const val STATE_JSON_SHORTCUT = "shortcut_json"
        private const val STATE_INITIAL_ICON = "initial_icon"

    }

}
