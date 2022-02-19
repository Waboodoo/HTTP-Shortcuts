package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable

class OpenURLAction(private val url: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            try {
                Intent(Intent.ACTION_VIEW, url.toUri())
                    .startActivity(executionContext.context)
            } catch (e: ActivityNotFoundException) {
                throw ActionException { context ->
                    context.getString(R.string.error_no_app_found_for_url, url)
                }
            }
        }
}
