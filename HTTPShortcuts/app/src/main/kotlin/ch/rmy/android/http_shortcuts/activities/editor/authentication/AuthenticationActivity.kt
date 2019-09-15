package ch.rmy.android.http_shortcuts.activities.editor.authentication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class AuthenticationActivity : BaseActivity() {

    private val viewModel: AuthenticationViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private val authenticationMethodSpinner: LabelledSpinner by bindView(R.id.input_authentication_method)
    private val usernameView: VariableEditText by bindView(R.id.input_username)
    private val usernameVariableButton: VariableButton by bindView(R.id.variable_button_username)
    private val usernameContainer: View by bindView(R.id.container_username)
    private val passwordView: VariableEditText by bindView(R.id.input_password)
    private val passwordVariableButton: VariableButton by bindView(R.id.variable_button_password)
    private val passwordContainer: View by bindView(R.id.container_password)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        authenticationMethodSpinner.setItemsFromPairs(AUTHENTICATION_METHODS.map {
            it.first to getString(it.second)
        })
        VariableViewUtils.bindVariableViews(usernameView, usernameVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
        VariableViewUtils.bindVariableViews(passwordView, passwordVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
        variablesData.observe(this, Observer {
            updateShortcutViews()
        })

        authenticationMethodSpinner.selectionChanges
            .concatMapCompletable { method -> viewModel.setAuthenticationMethod(method) }
            .subscribe()
            .attachTo(destroyer)
        bindTextChangeListener(usernameView) { shortcutData.value?.username }
        bindTextChangeListener(passwordView) { shortcutData.value?.password }
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
        viewModel.setCredentials(usernameView.rawString, passwordView.rawString)

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        authenticationMethodSpinner.selectedItem = shortcut.authentication ?: Shortcut.AUTHENTICATION_NONE
        if (shortcut.usesAuthentication()) {
            usernameContainer.visible = true
            passwordContainer.visible = true
            usernameView.rawString = shortcut.username
            passwordView.rawString = shortcut.password
        } else {
            usernameContainer.visible = false
            passwordContainer.visible = false
        }
    }

    override fun onBackPressed() {
        updateViewModelFromViews()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AuthenticationActivity::class.java)

    companion object {

        private val AUTHENTICATION_METHODS = listOf(
            Shortcut.AUTHENTICATION_NONE to R.string.authentication_none,
            Shortcut.AUTHENTICATION_BASIC to R.string.authentication_basic,
            Shortcut.AUTHENTICATION_DIGEST to R.string.authentication_digest
        )

    }

}