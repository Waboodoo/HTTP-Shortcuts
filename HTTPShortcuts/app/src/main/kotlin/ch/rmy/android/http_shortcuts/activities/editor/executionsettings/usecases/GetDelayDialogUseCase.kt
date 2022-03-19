package ch.rmy.android.http_shortcuts.activities.editor.executionsettings.usecases

import android.app.Dialog
import android.content.Context
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.framework.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class GetDelayDialogUseCase {

    operator fun invoke(
        delay: Duration,
        getLabel: (Duration) -> Localizable,
        onDelayChanged: (timeout: Duration) -> Unit,
    ): DialogState =
        object : DialogState {
            override fun createDialog(context: Context, viewModel: WithDialog): Dialog =
                DialogBuilder(context)
                    .title(R.string.label_delay_execution)
                    .view(R.layout.dialog_time_picker)
                    .positive(R.string.dialog_ok) {
                        val slider = it.findViewById<SeekBar>(R.id.slider)
                        onDelayChanged(progressToDelay(slider.progress))
                    }
                    .dismissListener {
                        viewModel.onDialogDismissed(this)
                    }
                    .build()
                    .show {
                        val slider = findViewById<SeekBar>(R.id.slider)
                        val label = findViewById<TextView>(R.id.slider_value)

                        slider.max = DELAY_OPTIONS.lastIndex
                        slider.setOnSeekBarChangeListener(
                            object : SimpleOnSeekBarChangeListener() {
                                override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                                    label.text = getLabel(progressToDelay(progress)).localize(context)
                                }
                            })
                        label.text = getLabel(delay).localize(context)
                        slider.progress = delayToProgress(delay)
                    }
        }

    companion object {

        private val DELAY_OPTIONS = arrayOf(
            0.milliseconds,
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
            15.minutes,
            20.minutes,
            30.minutes,
            1.hours,
        )

        private fun delayToProgress(delay: Duration) =
            DELAY_OPTIONS
                .indexOfFirst {
                    it >= delay
                }
                .takeUnless { it == -1 }
                ?: DELAY_OPTIONS.lastIndex

        private fun progressToDelay(progress: Int) =
            DELAY_OPTIONS[progress]
    }
}
