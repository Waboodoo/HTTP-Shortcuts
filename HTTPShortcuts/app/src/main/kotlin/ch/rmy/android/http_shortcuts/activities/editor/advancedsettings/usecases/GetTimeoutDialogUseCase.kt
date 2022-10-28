package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.usecases

import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.framework.extensions.setText
import ch.rmy.android.framework.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GetTimeoutDialogUseCase
@Inject
constructor() {

    operator fun invoke(
        timeout: Duration,
        getLabel: (Duration) -> Localizable,
        onTimeoutChanged: (timeout: Duration) -> Unit,
    ): DialogState =
        createDialogState(id = "timeout-dialog") {
            title(R.string.label_timeout)
                .view(R.layout.dialog_time_picker)
                .positive(R.string.dialog_ok) {
                    val slider = it.findViewById<SeekBar>(R.id.slider)
                    onTimeoutChanged(progressToTimeout(slider.progress))
                }
                .build()
                .show {
                    val slider = findViewById<SeekBar>(R.id.slider)
                    val label = findViewById<TextView>(R.id.slider_value)

                    slider.max = TIMEOUT_OPTIONS.lastIndex
                    slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener {
                        override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                            label.text = getLabel(progressToTimeout(progress)).localize(context)
                        }
                    })
                    label.setText(getLabel(timeout))
                    slider.progress = timeoutToProgress(timeout)
                }
        }

    companion object {

        private val TIMEOUT_OPTIONS = arrayOf(
            500.milliseconds,
            1.seconds,
            2.seconds,
            3.seconds,
            5.seconds,
            8.seconds,
            10.seconds,
            15.seconds,
            20.seconds,
            25.seconds,
            30.seconds,
            45.seconds,
            1.minutes,
            90.seconds,
            2.minutes,
            3.minutes,
            5.minutes,
            450.seconds,
            10.minutes,
        )

        private fun timeoutToProgress(timeout: Duration) =
            TIMEOUT_OPTIONS.indexOfFirst {
                it >= timeout
            }
                .takeUnless { it == -1 }
                ?: TIMEOUT_OPTIONS.lastIndex

        private fun progressToTimeout(progress: Int) =
            TIMEOUT_OPTIONS[progress]
    }
}
