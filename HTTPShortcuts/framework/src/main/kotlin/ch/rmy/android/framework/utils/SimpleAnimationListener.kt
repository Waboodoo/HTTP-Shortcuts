package ch.rmy.android.framework.utils

import android.view.animation.Animation

interface SimpleAnimationListener : Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation) {
    }

    override fun onAnimationEnd(animation: Animation) {
    }

    override fun onAnimationRepeat(animation: Animation) {
    }
}
