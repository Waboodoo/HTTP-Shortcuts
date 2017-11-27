package ch.rmy.android.http_shortcuts.activities

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.IconNameChangeDialog
import ch.rmy.android.http_shortcuts.icons.IconSelector
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.key_value_pairs.KeyValueList
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Header
import ch.rmy.android.http_shortcuts.realm.models.Parameter
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.realm.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.utils.ArrayUtil
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener
import ch.rmy.android.http_shortcuts.utils.ShortcutUIUtils
import ch.rmy.android.http_shortcuts.utils.UIUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.utils.visible
import ch.rmy.android.http_shortcuts.variables.VariableFormatter
import ch.rmy.curlcommand.CurlCommand
import com.afollestad.materialdialogs.MaterialDialog
import com.satsuware.usefulviews.LabelledSpinner
import kotterknife.bindView
import siclo.com.ezphotopicker.api.EZPhotoPick
import siclo.com.ezphotopicker.api.EZPhotoPickStorage
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig
import siclo.com.ezphotopicker.api.models.PhotoSource
import java.io.IOException
import java.io.OutputStream

@SuppressLint("InflateParams")
class EditorActivity : BaseActivity() {

    private var shortcutId: Long = 0

    private val controller by lazy { destroyer.own(Controller()) }
    private val variables by lazy { controller.variables }

    private lateinit var oldShortcut: Shortcut
    private lateinit var shortcut: Shortcut

    private val methodView: LabelledSpinner by bindView(R.id.input_method)
    private val feedbackView: LabelledSpinner by bindView(R.id.input_feedback)
    private val timeoutView: LabelledSpinner by bindView(R.id.input_timeout)
    private val retryPolicyView: LabelledSpinner by bindView(R.id.input_retry_policy)
    private val nameView: EditText by bindView(R.id.input_shortcut_name)
    private val descriptionView: EditText by bindView(R.id.input_description)
    private val urlView: EditText by bindView(R.id.input_url)
    private val authenticationView: LabelledSpinner by bindView(R.id.input_authentication)
    private val usernameView: EditText by bindView(R.id.input_username)
    private val passwordView: EditText by bindView(R.id.input_password)
    private val iconView: IconView by bindView(R.id.input_icon)
    private val parameterList: KeyValueList<Parameter> by bindView(R.id.post_parameter_list)
    private val customHeaderList: KeyValueList<Header> by bindView(R.id.custom_headers_list)
    private val customBodyView: EditText by bindView(R.id.input_custom_body)
    private val acceptCertificatesCheckbox: CheckBox by bindView(R.id.input_accept_all_certificates)
    private val authenticationContainer: LinearLayout by bindView(R.id.authentication_container)
    private val requestBodyContainer: LinearLayout by bindView(R.id.section_request_body)
    private val launcherShortcutCheckbox: CheckBox by bindView(R.id.input_launcher_shortcut)
    private val delayView: LabelledSpinner by bindView(R.id.input_delay_execution)

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

        destroyer.own(parameterList)
        destroyer.own(customHeaderList)

        shortcutId = intent.getLongExtra(EXTRA_SHORTCUT_ID, 0)
        val shortcut = if (savedInstanceState != null && savedInstanceState.containsKey(STATE_JSON_SHORTCUT)) {
            GsonUtil.fromJson(savedInstanceState.getString(STATE_JSON_SHORTCUT)!!, Shortcut::class.java)
        } else {
            if (shortcutId == 0L) Shortcut.createNew() else controller.getDetachedShortcutById(shortcutId)
        }
        if (shortcut == null) {
            finish()
            return
        }
        this.shortcut = shortcut
        oldShortcut = (if (shortcutId != 0L) controller.getDetachedShortcutById(shortcutId) else null) ?: Shortcut.createNew()

        if (shortcut.isNew) {

            val curlCommand = intent.getSerializableExtra(EXTRA_CURL_COMMAND)
            if (curlCommand != null) {
                extractFromCurlCommand(shortcut, curlCommand as CurlCommand)
            }

            if (shortcut.iconName == null) {
                shortcut.iconName = Icons.getRandomIcon(context)
                oldShortcut.iconName = shortcut.iconName
            } else if (savedInstanceState != null && savedInstanceState.containsKey(STATE_INITIAL_ICON)) {
                oldShortcut.iconName = savedInstanceState.getString(STATE_INITIAL_ICON)
            }
        }

        initViews()
    }

    private fun extractFromCurlCommand(shortcut: Shortcut, curlCommand: CurlCommand) {
        shortcut.url = curlCommand.url
        shortcut.method = curlCommand.method
        shortcut.bodyContent = curlCommand.data
        shortcut.username = curlCommand.username
        shortcut.password = curlCommand.password
        if (!TextUtils.isEmpty(curlCommand.username) || !TextUtils.isEmpty(curlCommand.password)) {
            shortcut.authentication = Shortcut.AUTHENTICATION_BASIC
        }
        if (curlCommand.timeout != 0) {
            shortcut.timeout = curlCommand.timeout
        }
        for ((key, value) in curlCommand.headers) {
            shortcut.headers!!.add(Header.createNew(key, value))
        }
    }

    private fun initViews() {
        nameView.setText(shortcut.name)
        descriptionView.setText(shortcut.description)
        urlView.setText(shortcut.url)
        usernameView.setText(shortcut.username)
        passwordView.setText(shortcut.password)
        customBodyView.setText(shortcut.bodyContent)

        bindVariableFormatter(urlView)
        bindVariableFormatter(usernameView)
        bindVariableFormatter(passwordView)
        bindVariableFormatter(customBodyView)

        methodView.setItemsArray(Shortcut.METHODS)
        UIUtil.fixLabelledSpinner(methodView)
        methodView.onItemChosenListener = itemChosenListener
        methodView.setSelection(ArrayUtil.findIndex(Shortcut.METHODS, shortcut.method))

        authenticationView.setItemsArray(ShortcutUIUtils.getAuthenticationOptions(context))
        UIUtil.fixLabelledSpinner(authenticationView)
        authenticationView.onItemChosenListener = itemChosenListener
        authenticationView.setSelection(ArrayUtil.findIndex(Shortcut.AUTHENTICATION_OPTIONS, shortcut.authentication!!))

        parameterList.addItems(shortcut.parameters!!)
        parameterList.setButtonText(R.string.button_add_post_param)
        parameterList.setAddDialogTitle(R.string.title_post_param_add)
        parameterList.setEditDialogTitle(R.string.title_post_param_edit)
        parameterList.setKeyLabel(R.string.label_post_param_key)
        parameterList.setValueLabel(R.string.label_post_param_value)
        parameterList.setItemFactory({ key, value -> Parameter.createNew(key, value) })

        customHeaderList.addItems(shortcut.headers!!)
        customHeaderList.setButtonText(R.string.button_add_custom_header)
        customHeaderList.setAddDialogTitle(R.string.title_custom_header_add)
        customHeaderList.setEditDialogTitle(R.string.title_custom_header_edit)
        customHeaderList.setKeyLabel(R.string.label_custom_header_key)
        customHeaderList.setValueLabel(R.string.label_custom_header_value)
        customHeaderList.setItemFactory({ key, value -> Header.createNew(key, value) })
        customHeaderList.setSuggestions(Header.SUGGESTED_KEYS)

        feedbackView.setItemsArray(ShortcutUIUtils.getFeedbackOptions(context))
        feedbackView.onItemChosenListener = itemChosenListener
        UIUtil.fixLabelledSpinner(feedbackView)
        feedbackView.setSelection(ArrayUtil.findIndex(Shortcut.FEEDBACK_OPTIONS, shortcut.feedback!!))

        timeoutView.setItemsArray(ShortcutUIUtils.getTimeoutOptions(context))
        UIUtil.fixLabelledSpinner(timeoutView)
        timeoutView.setSelection(ArrayUtil.findIndex(Shortcut.TIMEOUT_OPTIONS, shortcut.timeout))

        retryPolicyView.setItemsArray(ShortcutUIUtils.getRetryPolicyOptions(context))
        UIUtil.fixLabelledSpinner(retryPolicyView)
        retryPolicyView.setSelection(ArrayUtil.findIndex(Shortcut.RETRY_POLICY_OPTIONS, shortcut.retryPolicy!!))

        acceptCertificatesCheckbox.isChecked = shortcut.acceptAllCertificates
        launcherShortcutCheckbox.isChecked = shortcut.launcherShortcut

        delayView.setItemsArray(ShortcutUIUtils.getDelayOptions(context))
        UIUtil.fixLabelledSpinner(delayView)
        delayView.setSelection(ArrayUtil.findIndex(Shortcut.DELAY_OPTIONS, shortcut.delay))

        iconView.setOnClickListener { openIconSelectionDialog() }

        setTitle(if (shortcut.isNew) R.string.create_shortcut else R.string.edit_shortcut)
        updateUI()
    }

    private fun bindVariableFormatter(editText: EditText) {
        destroyer.own(VariableFormatter.bind(editText, variables))
    }

    private fun updateUI() {
        iconView.setImageURI(shortcut.getIconURI(this), shortcut.iconName)
        retryPolicyView.visible = shortcut.isRetryAllowed()
        requestBodyContainer.visible = shortcut.allowsBody()
        authenticationContainer.visible = shortcut.usesAuthentication()
        launcherShortcutCheckbox.visible = LauncherShortcutManager.supportsLauncherShortcuts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                confirmClose()
                true
            }
            R.id.action_save_shortcut -> {
                trySave()
                true
            }
            R.id.action_test_shortcut -> {
                test()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun trySave() {
        compileShortcut()
        if (validate(false)) {
            shortcut.id = shortcutId
            val persistedShortcut = controller.persist(shortcut)
            val returnIntent = Intent()
            returnIntent.putExtra(EXTRA_SHORTCUT_ID, persistedShortcut.id)
            setResult(Activity.RESULT_OK, returnIntent)
            val dialog = IconNameChangeDialog(this)
            if (!oldShortcut.isNew && nameOrIconChanged() && dialog.shouldShow()) {
                dialog.show(MaterialDialog.SingleButtonCallback { _, _ -> finish() })
            } else {
                finish()
            }
        }
    }

    private fun validate(testOnly: Boolean): Boolean {
        if (!testOnly && Validation.isEmpty(shortcut.name!!)) {
            nameView.error = getString(R.string.validation_name_not_empty)
            UIUtil.focus(nameView)
            return false
        }
        if (!Validation.isAcceptableUrl(shortcut.url!!)) {
            urlView.error = getString(R.string.validation_url_invalid)
            UIUtil.focus(urlView)
            return false
        }
        return true
    }

    private fun nameOrIconChanged() =
            oldShortcut.name != shortcut.name || oldShortcut.iconName != shortcut.iconName

    private fun test() {
        compileShortcut()
        if (validate(true)) {
            shortcut.id = TEMPORARY_ID
            controller.persist(shortcut)
            val intent = IntentUtil.createIntent(this, TEMPORARY_ID)
            startActivity(intent)
        }
    }

    private fun openIconSelectionDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.change_icon)
                .items(R.array.context_menu_choose_icon)
                .itemsCallback { _, _, which, _ ->
                    when (which) {
                        0 -> openBuiltInIconSelectionDialog()
                        1 -> openImagePicker()
                        2 -> openIpackPicker()
                    }
                }
                .show()
    }

    private fun openBuiltInIconSelectionDialog() {
        val iconSelector = IconSelector(this) { iconName ->
            shortcut.iconName = iconName
            updateUI()
        }
        iconSelector.show()
    }

    private fun openImagePicker() {
        val config = EZPhotoPickConfig()
        config.photoSource = PhotoSource.GALLERY
        config.permisionDeniedErrorStringResource = R.string.error_cannot_get_image_permission_denied
        config.unexpectedErrorStringResource = R.string.error_set_image
        EZPhotoPick.startPhotoPickActivity(this, config)
    }

    private fun openIpackPicker() {
        val iconIntent = IpackUtil.getIpackIntent(context)
        startActivityForResult(iconIntent, SELECT_IPACK_ICON)
    }

    override fun onBackPressed() {
        confirmClose()
    }

    private fun confirmClose() {
        compileShortcut()
        if (hasChanges()) {
            MaterialDialog.Builder(this)
                    .content(R.string.confirm_discard_changes_message)
                    .positiveText(R.string.dialog_discard)
                    .onPositive { _, _ -> cancelAndClose() }
                    .negativeText(R.string.dialog_cancel)
                    .show()
        } else {
            cancelAndClose()
        }
    }

    private fun hasChanges() = !oldShortcut.isSameAs(shortcut)

    private fun compileShortcut() {
        shortcut.name = nameView.text.toString().trim { it <= ' ' }
        shortcut.url = urlView.text.toString()
        shortcut.method = Shortcut.METHODS[methodView.spinner.selectedItemPosition]
        shortcut.description = descriptionView.text.toString().trim { it <= ' ' }
        shortcut.password = passwordView.text.toString()
        shortcut.username = usernameView.text.toString()
        shortcut.bodyContent = customBodyView.text.toString()
        shortcut.feedback = Shortcut.FEEDBACK_OPTIONS[feedbackView.spinner.selectedItemPosition]
        shortcut.timeout = Shortcut.TIMEOUT_OPTIONS[timeoutView.spinner.selectedItemPosition]
        shortcut.delay = Shortcut.DELAY_OPTIONS[delayView.spinner.selectedItemPosition]
        shortcut.authentication = Shortcut.AUTHENTICATION_OPTIONS[authenticationView.spinner.selectedItemPosition]
        shortcut.retryPolicy = Shortcut.RETRY_POLICY_OPTIONS[retryPolicyView.spinner.selectedItemPosition]
        shortcut.acceptAllCertificates = acceptCertificatesCheckbox.isChecked
        shortcut.launcherShortcut = launcherShortcutCheckbox.isChecked

        shortcut.parameters!!.clear()
        shortcut.parameters!!.addAll(parameterList.items)
        shortcut.headers!!.clear()
        shortcut.headers!!.addAll(customHeaderList.items)
    }

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE && intent != null) {
            val iconName = UUIDUtils.create() + ".png"
            var outStream: OutputStream? = null
            try {
                val bitmap = EZPhotoPickStorage(this).loadLatestStoredPhotoBitmap()
                val bitmapSize = iconSize
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmapSize, bitmapSize, true)
                if (bitmap != resizedBitmap) {
                    bitmap.recycle()
                }

                outStream = openFileOutput(iconName, 0)
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream?.flush()

                shortcut.iconName = iconName
            } catch (e: Exception) {
                CrashReporting.logException(e)
                shortcut.iconName = null
                showSnackbar(getString(R.string.error_set_image))
            } finally {
                try {
                    outStream?.close()
                } catch (e: IOException) {
                }
            }
        } else if (requestCode == SELECT_IPACK_ICON && intent != null) {
            val uri = IpackUtil.getIpackUri(intent)
            shortcut.iconName = uri.toString()
        }
        updateUI()
    }

    private val iconSize: Int
        get() {
            val appIconSize = resources.getDimension(android.R.dimen.app_icon_size).toInt()
            val launcherIconSize = if (Build.VERSION.SDK_INT >= 11) launcherLargeIconSize else 0
            return if (launcherIconSize > appIconSize) launcherIconSize else appIconSize
        }

    private val launcherLargeIconSize: Int
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        get() {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return activityManager.launcherLargeIconSize
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        compileShortcut()
        outState.putString(STATE_JSON_SHORTCUT, GsonUtil.toJson(shortcut))
        outState.putString(STATE_INITIAL_ICON, oldShortcut.iconName)
    }

    companion object {

        const val EXTRA_SHORTCUT_ID = "ch.rmy.android.http_shortcuts.activities.EditorActivity.shortcut_id"
        const val EXTRA_CURL_COMMAND = "ch.rmy.android.http_shortcuts.activities.EditorActivity.curl_command"

        private const val SELECT_ICON = 1
        private const val SELECT_IPACK_ICON = 3
        private const val STATE_JSON_SHORTCUT = "shortcut_json"
        private const val STATE_INITIAL_ICON = "initial_icon"
    }
}
