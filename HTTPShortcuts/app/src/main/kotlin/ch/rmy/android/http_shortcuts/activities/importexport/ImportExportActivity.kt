package ch.rmy.android.http_shortcuts.activities.importexport

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImportExportActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        ImportExportScreen(
            ImportExportViewModel.InitData(
                importUrl = intent.getParcelable(EXTRA_IMPORT_URL),
            ),
        )
    }

    object OpenImportExport : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {

        private const val EXTRA_CATEGORIES_CHANGED = "categories_changed"

        override fun parseResult(resultCode: Int, intent: Intent?) =
            intent?.getBooleanExtra(EXTRA_CATEGORIES_CHANGED, false) ?: false

        fun createResult(categoriesChanged: Boolean) =
            createIntent {
                putExtra(EXTRA_CATEGORIES_CHANGED, categoriesChanged)
            }
    }

    class IntentBuilder : BaseIntentBuilder(ImportExportActivity::class) {
        fun importUrl(importUrl: Uri) = also {
            intent.putExtra(EXTRA_IMPORT_URL, importUrl)
        }
    }

    companion object {
        private const val EXTRA_IMPORT_URL = "ch.rmy.android.http_shortcuts.import_url"
    }
}
