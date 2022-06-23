package ch.rmy.android.framework.utils

import android.widget.SeekBar

interface SimpleOnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

    override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(slider: SeekBar) {
    }

    override fun onStopTrackingTouch(slider: SeekBar) {
    }
}
