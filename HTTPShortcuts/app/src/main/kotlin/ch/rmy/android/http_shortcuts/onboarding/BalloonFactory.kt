package ch.rmy.android.http_shortcuts.onboarding

import android.app.Activity
import android.content.Context
import com.skydoves.balloon.ArrowConstraints
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon

class BalloonFactory(private val context: Context, activity: Activity) {

    fun shouldShowBalloons(): Boolean {
        return true
    }

    fun marskAsShown() {

    }

    fun createWelcomeBalloon(): Balloon {
        return Balloon.Builder(context)
            .setText("Hello World")
            .setTextSize(14f)
            .setPadding(16)
            .setArrowVisible(false)
            .build()
    }

    fun createMenuBalloon(): Balloon {
        return Balloon.Builder(context)
            .setText("Click here to get started. It's gonna be fun!")
            .setPadding(16)
            .setTextSize(14f)
            .setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
            .setArrowOrientation(ArrowOrientation.TOP)
            .build()
    }

    fun createFABBalloon(): Balloon {
        return Balloon.Builder(context)
            .setText("Click here to get started creating new shortcuts")
            .setPadding(16)
            .setTextSize(14f)
            .setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
            .setArrowOrientation(ArrowOrientation.BOTTOM)
            .build()
    }
}