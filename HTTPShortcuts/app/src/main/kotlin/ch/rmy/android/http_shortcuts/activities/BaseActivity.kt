package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseActivity
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.documentation.DocumentationActivity
import ch.rmy.android.http_shortcuts.activities.settings.documentation.DocumentationUrlManager
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.ThemeHelper

abstract class BaseActivity : BaseActivity() {

    val themeHelper by lazy(LazyThreadSafetyMode.NONE) {
        ThemeHelper(context)
    }

    open val initializeWithTheme: Boolean
        get() = true

    final override fun onCreate(savedInstanceState: Bundle?) {
        inject(getApplicationComponent())
        if (initializeWithTheme) {
            setTheme(themeHelper.theme)
        }
        super.onCreate(savedInstanceState)
        try {
            RealmFactory.init(applicationContext)
            onCreated(savedInstanceState)
        } catch (e: RealmFactory.RealmNotFoundException) {
            if (this is Entrypoint) {
                showRealmError()
            } else {
                throw e
            }
        }
    }

    protected open fun inject(applicationComponent: ApplicationComponent) {
        // intentionally left blank
    }

    protected open fun onCreated(savedState: Bundle?) {
        // intentionally left blank
    }

    override fun onStart() {
        super.onStart()
        ActivityProvider.registerActivity(this)
    }

    override fun onStop() {
        super.onStop()
        ActivityProvider.deregisterActivity(this)
    }

    private fun showRealmError() {
        DialogBuilder(this)
            .title(R.string.dialog_title_error)
            .message(R.string.error_realm_unavailable, isHtml = true)
            .positive(R.string.dialog_ok)
            .dismissListener {
                finish()
            }
            .showOrElse {
                finish()
            }
    }

    override fun computeStatusBarColor(): Int =
        themeHelper.statusBarColor

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ViewModelEvent.OpenURL -> {
                val uri = event.url.toUri()
                if (DocumentationUrlManager.canHandle(uri)) {
                    DocumentationActivity.IntentBuilder()
                        .url(uri)
                        .startActivity(this)
                } else {
                    super.handleEvent(event)
                }
            }
            else -> super.handleEvent(event)
        }
    }
}
