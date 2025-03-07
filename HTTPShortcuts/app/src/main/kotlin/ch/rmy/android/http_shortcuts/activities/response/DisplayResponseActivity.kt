package ch.rmy.android.http_shortcuts.activities.response

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.utils.ActivityCloser
import dagger.hilt.android.AndroidEntryPoint
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisplayResponseActivity : BaseComposeActivity() {

    private lateinit var responseDataId: NavigationArgStore.ArgStoreId

    override fun onCreated(savedState: Bundle?) {
        val responseDataId = intent?.extras
            ?.getString(EXTRA_RESPONSE_DATA_ID)
            ?.let { NavigationArgStore.ArgStoreId(it) }
        if (responseDataId == null) {
            finishWithoutAnimation()
        } else {
            this.responseDataId = responseDataId
            super.onCreated(savedState)
        }
    }

    @Composable
    override fun Content() {
        DisplayResponseScreen(
            title = intent?.extras?.getString(EXTRA_TITLE) ?: "",
            responseDataId = responseDataId,
        )
    }

    private var autoFinishJob: Job? = null
    private var suppressAutoFinish = false

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is DisplayResponseEvent.SuppressAutoFinish -> suppressAutoFinish = true
            else -> super.handleEvent(event)
        }
    }

    override fun onStart() {
        super.onStart()
        suppressAutoFinish = false
        autoFinishJob?.cancel()
        autoFinishJob = null
    }

    override fun onStop() {
        super.onStop()
        if (!suppressAutoFinish) {
            autoFinishJob = lifecycleScope.launch {
                delay(FINISH_DELAY)
                finish()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        ActivityCloser.onDisplayResponseActivityClosed()
        finish()
    }

    class IntentBuilder(title: String, responseDataId: NavigationArgStore.ArgStoreId) : BaseIntentBuilder(DisplayResponseActivity::class) {
        init {
            intent.putExtra(EXTRA_TITLE, title)
            intent.putExtra(EXTRA_RESPONSE_DATA_ID, responseDataId.toString())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    companion object {
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_RESPONSE_DATA_ID = "response_data_id"

        private val FINISH_DELAY = 8.seconds
    }
}
