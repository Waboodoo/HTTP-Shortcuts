package ch.rmy.android.http_shortcuts.activities.editor

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
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationActivity
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyActivity
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersActivity
import ch.rmy.android.http_shortcuts.activities.editor.miscsettings.MiscSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.postrequest.PostRequestActivity
import ch.rmy.android.http_shortcuts.activities.editor.prerequest.PreRequestActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.dialogs.IconNameChangeDialog
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.dimen
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconSelector
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.IconUtil
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.views.PanelButton
import ch.rmy.curlcommand.CurlCommand
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotterknife.bindView
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ShortcutEditorActivity : BaseActivity() {

    private val shortcutId by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }
    private val categoryId by lazy {
        intent.getStringExtra(EXTRA_CATEGORY_ID)
    }
    private val curlCommand by lazy {
        intent.getSerializableExtra(EXTRA_CURL_COMMAND) as CurlCommand?
    }
    private val createBrowserShortcut: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_BROWSER_SHORTCUT, false)
    }

    private val viewModel: ShortcutEditorViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    // Views
    private val iconView: IconView by bindView(R.id.input_icon)
    private val iconContainer: View by bindView(R.id.icon_container)
    private val nameView: EditText by bindView(R.id.input_shortcut_name)
    private val descriptionView: EditText by bindView(R.id.input_description)
    private val basicRequestSettingsButton: PanelButton by bindView(R.id.button_basic_request_settings)
    private val headersButton: PanelButton by bindView(R.id.button_headers)
    private val requestBodyButton: PanelButton by bindView(R.id.button_request_body)
    private val authenticationButton: PanelButton by bindView(R.id.button_authentication)
    private val preRequestActionsButton: PanelButton by bindView(R.id.button_pre_request_actions)
    private val postRequestActionsButton: PanelButton by bindView(R.id.button_post_request_actions)
    private val miscSettingsButton: PanelButton by bindView(R.id.button_misc_settings)
    private val advancedTechnicalSettingsButton: PanelButton by bindView(R.id.button_advanced_technical_settings)
    private val dividerBelowHeaders: View by bindView(R.id.divider_below_headers)
    private val dividerBelowRequestBody: View by bindView(R.id.divider_below_request_body)
    private val dividerBelowAuthentication: View by bindView(R.id.divider_below_authentication)
    private val dividerBelowPostActions: View by bindView(R.id.divider_below_post_request_actions)

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(if (shortcutId != null) {
            R.string.edit_shortcut
        } else {
            R.string.create_shortcut
        })
        setContentView(R.layout.activity_loading)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.init(categoryId, shortcutId, curlCommand, createBrowserShortcut)
            .subscribe({
                initViews()
            }, { e ->
                handleUnknownError(e)
                finish()
            })
            .attachTo(destroyer)
    }

    private fun initViews() {
        setContentView(R.layout.activity_shortcut_editor_overview)
        invalidateOptionsMenu()
        bindViewsToViewModel()
        bindClickListeners()
        bindTextChangeListeners()
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
            invalidateOptionsMenu()
        })
        variablesData.observe(this, Observer {
            updateShortcutViews()
        })
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        iconView.setImageURI(IconUtil.getIconURI(context, shortcut.iconName), shortcut.iconName, animated = true)
        nameView.setTextSafely(shortcut.name)
        descriptionView.setTextSafely(shortcut.description)

        val isBrowserShortcut = shortcut.isBrowserShortcut
        headersButton.visible = !isBrowserShortcut
        dividerBelowHeaders.visible = !isBrowserShortcut
        requestBodyButton.visible = !isBrowserShortcut
        dividerBelowRequestBody.visible = !isBrowserShortcut
        authenticationButton.visible = !isBrowserShortcut
        dividerBelowAuthentication.visible = !isBrowserShortcut
        postRequestActionsButton.visible = !isBrowserShortcut
        dividerBelowPostActions.visible = !isBrowserShortcut
        advancedTechnicalSettingsButton.visible = !isBrowserShortcut

        basicRequestSettingsButton.subtitle = viewModel.getBasicSettingsSubtitle(shortcut)
            .let { subtitle ->
                Variables.rawPlaceholdersToVariableSpans(
                    subtitle,
                    variablePlaceholderProvider,
                    variablePlaceholderColor
                )
            }

        if (!isBrowserShortcut) {
            headersButton.subtitle = viewModel.getHeadersSettingsSubtitle(shortcut)
            requestBodyButton.subtitle = viewModel.getRequestBodySettingsSubtitle(shortcut)
            authenticationButton.subtitle = viewModel.getAuthenticationSettingsSubtitle(shortcut)

            requestBodyButton.isEnabled = shortcut.allowsBody()
        }
    }

    private fun bindClickListeners() {
        iconContainer.setOnClickListener {
            openIconSelectionDialog()
        }
        basicRequestSettingsButton.setOnClickListener {
            BasicRequestSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        headersButton.setOnClickListener {
            RequestHeadersActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        requestBodyButton.setOnClickListener {
            RequestBodyActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        authenticationButton.setOnClickListener {
            AuthenticationActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        preRequestActionsButton.setOnClickListener {
            PreRequestActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        postRequestActionsButton.setOnClickListener {
            PostRequestActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        miscSettingsButton.setOnClickListener {
            MiscSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        advancedTechnicalSettingsButton.setOnClickListener {
            AdvancedSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
    }

    private fun bindTextChangeListeners() {
        bindTextChangeListener(nameView) { shortcutData.value?.name }
        bindTextChangeListener(descriptionView) { shortcutData.value?.description }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun openIconSelectionDialog() {
        DialogBuilder(context)
            .title(R.string.change_icon)
            .item(R.string.choose_icon, ::openBuiltInIconSelectionDialog)
            .item(R.string.choose_image, ::openImagePicker)
            .item(R.string.choose_ipack_icon, ::openIpackPicker)
            .showIfPossible()
    }

    private fun openBuiltInIconSelectionDialog() {
        IconSelector(context)
            .show()
            .flatMapCompletable { iconName ->
                viewModel.setIconName(iconName)
            }
            .subscribe()
            .attachTo(destroyer)
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
        IpackUtil.getIpackIntent(context)
            .startActivity(this, REQUEST_SELECT_IPACK_ICON)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (viewModel.isInitialized) {
            menuInflater.inflate(R.menu.editor_activity_menu, menu)
            menu.findItem(R.id.action_test_shortcut).isVisible = shortcutData.value?.url?.let { Validation.isAcceptableUrl(it) } == true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { onCloseEditor() }
        R.id.action_save_shortcut -> consume { trySaveShortcut() }
        R.id.action_test_shortcut -> consume { testShortcut() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onCloseEditor() {
        if (viewModel.isInitialized) {
            updateViewModelFromViews()
                .subscribe {
                    if (viewModel.hasChanges()) {
                        DialogBuilder(context)
                            .message(R.string.confirm_discard_changes_message)
                            .positive(R.string.dialog_discard) { cancelAndClose() }
                            .negative(R.string.dialog_cancel)
                            .showIfPossible()
                    } else {
                        cancelAndClose()
                    }
                }
                .attachTo(destroyer)
        } else {
            cancelAndClose()
        }
    }

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun trySaveShortcut() {
        updateViewModelFromViews()
            .andThen(viewModel.trySave())
            .observeOn(mainThread())
            .subscribe({ saveResult ->
                LauncherShortcutManager.updatePinnedShortcut(context, saveResult.id, saveResult.name, saveResult.iconName)
                setResult(RESULT_OK, Intent().putExtra(RESULT_SHORTCUT_ID, saveResult.id))
                onSaveComplete(saveResult.nameOrIconChanged)
            }, { e ->
                if (e is ShortcutValidationError) {
                    when (e.type) {
                        ShortcutEditorViewModel.VALIDATION_ERROR_EMPTY_NAME -> {
                            showSnackbar(R.string.validation_name_not_empty, long = true)
                            nameView.focus()
                        }
                        ShortcutEditorViewModel.VALIDATION_ERROR_INVALID_URL -> {
                            showSnackbar(R.string.validation_url_invalid, long = true)
                        }
                        else -> handleUnknownError(e)
                    }
                } else {
                    handleUnknownError(e)
                }
            })
            .attachTo(destroyer)
    }

    private fun onSaveComplete(nameOrIconChanged: Boolean) {
        if (nameOrIconChanged) {
            IconNameChangeDialog(context)
                .showIfNeeded()
                .subscribe {
                    finish()
                }
                .attachTo(destroyer)
        } else {
            finish()
        }
    }

    private fun testShortcut() {
        updateViewModelFromViews()
            .subscribe {
                ExecuteActivity.IntentBuilder(context, Shortcut.TEMPORARY_ID)
                    .build()
                    .startActivity(this)
            }
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setNameAndDescription(
            name = nameView.text.toString(),
            description = descriptionView.text.toString()
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                try {
                    val result = CropImage.getActivityResult(intent)
                    if (resultCode == RESULT_OK) {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.uri)
                        val iconName = "${newUUID()}.png"
                        openFileOutput(iconName, 0).use {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                            it.flush()
                        }
                        bitmap.recycle()
                        updateIconName(iconName)
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        if (result.error != null) {
                            logException(result.error)
                        }
                        showSnackbar(R.string.error_set_image, long = true)
                    }
                } catch (e: Exception) {
                    logException(e)
                    showSnackbar(R.string.error_set_image, long = true)
                }
            }
            REQUEST_SELECT_IPACK_ICON -> {
                if (resultCode == RESULT_OK && intent != null) {
                    updateIconName(IpackUtil.getIpackUri(intent).toString())
                }
            }
        }
    }

    private fun updateIconName(iconName: String) {
        viewModel.setIconName(iconName)
            .subscribe()
            .attachTo(destroyer)
    }

    override fun onBackPressed() {
        onCloseEditor()
    }

    private val iconSize by lazy {
        max(dimen(android.R.dimen.app_icon_size), launcherLargeIconSize)
    }

    private val launcherLargeIconSize: Int
        get() = (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconSize

    private fun handleUnknownError(e: Throwable) {
        logException(e)
        showToast(R.string.error_generic)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ShortcutEditorActivity::class.java) {

        fun shortcutId(shortcutId: String) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun categoryId(categoryId: String) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }

        fun curlCommand(command: CurlCommand) = also {
            intent.putExtra(EXTRA_CURL_COMMAND, command)
        }

        fun browserShortcut(browserShortcut: Boolean) = also {
            intent.putExtra(EXTRA_BROWSER_SHORTCUT, browserShortcut)
        }

    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_CATEGORY_ID = "categoryId"
        private const val EXTRA_CURL_COMMAND = "curlCommand"
        private const val EXTRA_BROWSER_SHORTCUT = "browserShortcut"

        private const val REQUEST_SELECT_IPACK_ICON = 3

        const val RESULT_SHORTCUT_ID = "shortcutId"

    }

}