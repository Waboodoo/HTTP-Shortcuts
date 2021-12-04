package ch.rmy.android.http_shortcuts.activities.misc

import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.Entrypoint
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog
import ch.rmy.android.http_shortcuts.utils.HTMLUtil

class DeepLinkActivity : BaseActivity(), Entrypoint {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isRealmAvailable) {
            return
        }

        val deepLinkUrl = intent.data

        val shortcutIdOrName = deepLinkUrl
            ?.let { uri ->
                uri
                    .host
                    ?.takeUnless { it == "deep-link" }
                    ?: uri.lastPathSegment
            }
            ?: run {
                showMessageDialog(HTMLUtil.format(getString(R.string.instructions_deep_linking, EXAMPLE_URL))) {
                    finishWithoutAnimation()
                }
                return
            }

        val shortcut = DataSource.getShortcutByNameOrId(shortcutIdOrName) ?: run {
            showMessageDialog(getString(R.string.error_shortcut_not_found_for_deep_link, shortcutIdOrName)) {
                finishWithoutAnimation()
            }
            return
        }

        val variables = deepLinkUrl.queryParameterNames
            .filterNot { it.isEmpty() }
            .associateWith { key ->
                deepLinkUrl.getQueryParameter(key) ?: ""
            }

        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .variableValues(variables)
            .startActivity(this)
        finishWithoutAnimation()
    }

    companion object {
        private const val EXAMPLE_URL = "http-shortcuts://<b>&lt;Name/ID of Shortcut&gt;</b>"
    }
}
