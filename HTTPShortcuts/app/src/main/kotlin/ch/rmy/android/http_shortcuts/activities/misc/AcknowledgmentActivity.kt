package ch.rmy.android.http_shortcuts.activities.misc

import android.content.Context
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityAcknowledgmentsBinding
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder

class AcknowledgmentActivity : BaseActivity() {

    private lateinit var binding: ActivityAcknowledgmentsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryOrLog {
            binding = applyBinding(ActivityAcknowledgmentsBinding.inflate(layoutInflater))
            setTitle(R.string.title_licenses)
            binding.acknowledgmentsWebview.loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
        } ?: run {
            showToast(R.string.error_generic)
            finish()
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, AcknowledgmentActivity::class.java)

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"

    }

}
