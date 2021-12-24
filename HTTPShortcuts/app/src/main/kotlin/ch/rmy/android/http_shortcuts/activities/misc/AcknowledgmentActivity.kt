package ch.rmy.android.http_shortcuts.activities.misc

import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityAcknowledgmentsBinding

class AcknowledgmentActivity : BaseActivity() {

    private lateinit var binding: ActivityAcknowledgmentsBinding

    override fun onCreate() {
        tryOrLog {
            binding = applyBinding(ActivityAcknowledgmentsBinding.inflate(layoutInflater))
            setTitle(R.string.title_licenses)
            binding.acknowledgmentsWebview.loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
        }
            ?: run {
                showToast(R.string.error_generic)
                finish()
            }
    }

    class IntentBuilder : BaseIntentBuilder(AcknowledgmentActivity::class.java)

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"
    }
}
