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
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.dimen
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.icons.IconSelector
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.IpackUtil
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import ch.rmy.android.http_shortcuts.views.PanelButton
import com.afollestad.materialdialogs.MaterialDialog
import com.theartofdev.edmodo.cropper.CropImage
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotterknife.bindView

class ShortcutEditorActivity : BaseActivity() {

    private val shortcutId by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val categoryId by lazy {
        intent.getStringExtra(EXTRA_CATEGORY_ID)
    }

    private val viewModel: ShortcutEditorViewModel by bindViewModel()

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
        viewModel.init(categoryId, shortcutId)
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
    }

    private fun bindViewsToViewModel() {
        viewModel.shortcut.observe(this, Observer {
            it?.let(::updateShortcutViews)
        })
    }

    private fun updateShortcutViews(shortcut: Shortcut) {
        iconView.setImageURI(shortcut.getIconURI(context), shortcut.iconName)
        nameView.setText(shortcut.name)
        descriptionView.setText(shortcut.description)

        basicRequestSettingsButton.subtitle = viewModel.getBasicSettingsSubtitle(shortcut)
        headersButton.subtitle = viewModel.getHeadersSettingsSubtitle(shortcut)
        requestBodyButton.subtitle = viewModel.getRequestBodySettingsSubtitle(shortcut)
        authenticationButton.subtitle = viewModel.getAuthenticationSettingsSubtitle(shortcut)
        preRequestActionsButton.subtitle = viewModel.getPreRequestActionsSettingsSubtitle(shortcut)
        postRequestActionsButton.subtitle = viewModel.getPostRequestActionsSettingsSubtitle(shortcut)

        requestBodyButton.isEnabled = shortcut.allowsBody()
    }

    private fun bindClickListeners() {
        iconContainer.setOnClickListener {
            openIconSelectionDialog()
        }
        basicRequestSettingsButton.setOnClickListener {

        }
        headersButton.setOnClickListener {

        }
        requestBodyButton.setOnClickListener {

        }
        authenticationButton.setOnClickListener {

        }
        preRequestActionsButton.setOnClickListener {

        }
        postRequestActionsButton.setOnClickListener {

        }
        miscSettingsButton.setOnClickListener {

        }
        advancedTechnicalSettingsButton.setOnClickListener {

        }
    }

    private fun openIconSelectionDialog() {
        MenuDialogBuilder(context)
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
            .subscribe({ id ->
                setResult(RESULT_OK, Intent().putExtra(RESULT_SHORTCUT_ID, id))
                onSaveComplete()
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

    private fun onSaveComplete() {
        finish()
        /*val dialog = IconNameChangeDialog(context)
        if (LauncherShortcutManager.supportsPinning(context)) {
            LauncherShortcutManager.updatePinnedShortcut(context, persistedShortcut)
            finish()
        } else if (!oldShortcut.isNew && nameOrIconChanged() && dialog.shouldShow()) {
            dialog.show()
                .done {
                    finish()
                }
        } else {
            finish()
        }*/
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
        Math.max(dimen(android.R.dimen.app_icon_size), launcherLargeIconSize)
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

    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_CATEGORY_ID = "categoryId"

        private const val REQUEST_SELECT_IPACK_ICON = 3

        const val RESULT_SHORTCUT_ID = "shortcutId"

    }

}