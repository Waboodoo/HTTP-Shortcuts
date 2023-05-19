package ch.rmy.android.http_shortcuts.activities

import android.os.Bundle
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.BaseActivity
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.documentation.DocumentationActivity
import ch.rmy.android.http_shortcuts.activities.documentation.DocumentationUrlManager
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.utils.ActivityProvider

abstract class BaseActivity : BaseActivity() {

    open val initializeWithTheme: Boolean
        get() = true

    final override fun onCreate(savedInstanceState: Bundle?) {
        inject(getApplicationComponent())
        setTheme(if (initializeWithTheme) R.style.LightTheme else R.style.LightThemeTransparent)
        super.onCreate(savedInstanceState)
        RealmFactoryImpl.init(applicationContext)
        onCreated(savedInstanceState)
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
