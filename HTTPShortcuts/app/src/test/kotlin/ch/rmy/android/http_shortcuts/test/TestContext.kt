package ch.rmy.android.http_shortcuts.test

import android.content.Context
import android.content.ContextWrapper
import androidx.test.core.app.ApplicationProvider
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponentProvider
import com.nhaarman.mockitokotlin2.mock

class TestContext private constructor() : ContextWrapper(ApplicationProvider.getApplicationContext()), ApplicationComponentProvider {

    override fun getApplicationContext(): Context =
        this

    override val applicationComponent: ApplicationComponent by lazy {
        mock()
    }

    companion object {
        fun create() = TestContext()
    }
}
