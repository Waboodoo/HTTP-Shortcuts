package ch.rmy.android.framework.ui

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ch.rmy.android.framework.extensions.runIfNotNull

abstract class BaseActivityResultContract<T : IntentBuilder, Result>(
    private val createIntentBuilder: (() -> T),
) : ActivityResultContract<(T.() -> T)?, Result>() {

    override fun createIntent(context: Context, input: (T.() -> T)?): Intent =
        createIntentBuilder()
            .runIfNotNull(input) { it() }
            .build(context)
}
