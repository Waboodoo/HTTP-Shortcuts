package ch.rmy.android.http_shortcuts.activities.misc.host

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.awaitNonNull
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HostActivity : BaseComposeActivity() {
    override val initializeWithTheme: Boolean
        get() = false

    override fun onCreated(savedState: Bundle?) {
        super.onCreated(savedState)
        lifecycleScope.launch {
            activeClients.collectLatest {
                if (it <= 0) {
                    delay(500)
                    finishWithoutAnimation()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activityFlow.value = this
    }

    override fun onStop() {
        super.onStop()
        activityFlow.value = null
    }

    @Composable
    override fun Content() {
        val dialogState by dialogStateFlow.collectAsState()
        ExecuteDialogs(
            dialogState,
            onResult = { result ->
                val deferred = deferredResult
                deferredResult = null
                dialogStateFlow.value = null
                deferred?.complete(result)
            },
            onDismissed = {
                val deferred = deferredResult
                deferredResult = null
                dialogStateFlow.value = null
                deferred?.cancel()
            },
        )
    }

    companion object {
        private val dialogStateFlow = MutableStateFlow<ExecuteDialogState<*>?>(null)
        private var deferredResult: CompletableDeferred<Any>? = null
        private var activityFlow = MutableStateFlow<FragmentActivity?>(null)
        private var activeClients = MutableStateFlow(0)

        suspend fun showDialog(context: Context, dialogState: ExecuteDialogState<*>): Any {
            val deferred = CompletableDeferred<Any>()
            deferredResult?.cancel()
            deferredResult = deferred
            dialogStateFlow.value = dialogState
            try {
                start(context)
                return deferred.await()
            } finally {
                stop()
            }
        }

        suspend fun start(context: Context): FragmentActivity {
            activeClients.update { it + 1 }
            return activityFlow.value
                ?: run {
                    startActivity(context)
                    activityFlow.awaitNonNull()
                }
        }

        fun stop() {
            activeClients.update { (it - 1).coerceAtLeast(0) }
        }

        private fun startActivity(context: Context) {
            context.startActivity(
                Intent(context, HostActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        }
    }
}
