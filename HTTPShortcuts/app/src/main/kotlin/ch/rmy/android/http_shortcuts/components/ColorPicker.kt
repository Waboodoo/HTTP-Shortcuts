package ch.rmy.android.http_shortcuts.components

import android.graphics.Color
import android.view.LayoutInflater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.http_shortcuts.R
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorListener
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar

@Composable
fun ColorPicker(
    color: Int,
    onColorChanged: (Int) -> Unit,
) {
    val context = LocalContext.current
    val view = remember {
        LayoutInflater.from(context).inflate(R.layout.color_picker, null)
    }
    val colorPickerView = remember {
        view.findViewById<ColorPickerView>(R.id.colorPickerView)
    }
    val brightnessSliderBar = remember {
        view.findViewById<BrightnessSlideBar>(R.id.brightnessSlideBar)
    }
    var applyColor by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(color) {
        if (applyColor) {
            colorPickerView.selectByHsvColor(color)
        } else {
            applyColor = true
        }
    }

    AndroidView(
        factory = {
            colorPickerView.attachBrightnessSlider(brightnessSliderBar)
            colorPickerView.setInitialColor(color)
            var previousPureColor: Int = colorPickerView.pureColor
            colorPickerView.setColorListener(object : ColorListener {
                override fun onColorSelected(color: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val selectedColor = if (color == Color.BLACK && colorPickerView.pureColor != previousPureColor) {
                            colorPickerView.selectByHsvColor(colorPickerView.pureColor)
                            colorPickerView.pureColor
                        } else {
                            color
                        }
                        previousPureColor = colorPickerView.pureColor
                        applyColor = false
                        onColorChanged(selectedColor)
                    }
                }
            })
            view
        },
        update = NoOpUpdate,
        onReset = NoOpUpdate,
    )
}
