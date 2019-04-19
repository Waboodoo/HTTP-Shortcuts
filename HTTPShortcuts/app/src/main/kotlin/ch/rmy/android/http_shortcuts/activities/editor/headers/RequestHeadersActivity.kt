package ch.rmy.android.http_shortcuts.activities.editor.headers

import android.content.Context
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

class RequestHeadersActivity : BaseActivity() {

    private val viewModel: RequestHeadersViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_headers)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {

    }

    private fun bindViewsToViewModel() {

    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, RequestHeadersActivity::class.java)

}