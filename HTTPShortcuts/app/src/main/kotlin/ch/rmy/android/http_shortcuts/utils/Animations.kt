package ch.rmy.android.http_shortcuts.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import ch.rmy.android.http_shortcuts.R

object Animations {

    fun zoomSwap(view: View, action: () -> Unit) {
        val zoomOut = AnimationUtils.loadAnimation(view.context, R.anim.zoom_out)
        zoomOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                action.invoke()
                val zoomIn = AnimationUtils.loadAnimation(view.context, R.anim.zoom_in)
                view.startAnimation(zoomIn)
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit

            override fun onAnimationStart(animation: Animation?) = Unit
        })
        view.startAnimation(zoomOut)
    }

}