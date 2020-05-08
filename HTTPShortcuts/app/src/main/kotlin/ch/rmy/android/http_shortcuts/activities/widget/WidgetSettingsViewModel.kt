package ch.rmy.android.http_shortcuts.activities.widget

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class WidgetSettingsViewModel(application: Application) : AndroidViewModel(application) {

    val showLabel = MutableLiveData(true)
    val labelColor = MutableLiveData(Color.WHITE)

    val labelColorFormatted: String
        get() = String.format("#%06x", labelColor.value!! and 0xffffff)

}