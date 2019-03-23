package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.realm.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Completable
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
    private val nameView: EditText by bindView(R.id.input_shortcut_name)
    private val descriptionView: EditText by bindView(R.id.input_description)

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
                logException(e)
                showToast(R.string.error_generic)
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
        nameView.setText(shortcut.name)
        descriptionView.setText(shortcut.description)
    }

    private fun bindClickListeners() {

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
        if (viewModel.isInitialized && viewModel.hasChanges()) {
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

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun trySaveShortcut() {
        updateViewModelFromViews()
            .andThen(viewModel.trySave())
            .subscribe { id ->
                setResult(RESULT_OK, Intent().putExtra(RESULT_SHORTCUT_ID, id))
                finish()
            }
            .attachTo(destroyer)
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
        viewModel.updateShortcut(
            name = nameView.text.toString(),
            description = descriptionView.text.toString()
        )

    override fun onBackPressed() {
        onCloseEditor()
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

        const val RESULT_SHORTCUT_ID = "shortcutId"

    }

}