package ch.rmy.android.http_shortcuts.dagger

import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.work.ListenableWorker
import ch.rmy.android.http_shortcuts.Application

fun Context.getApplicationComponent(): ApplicationComponent =
    (applicationContext as Application).applicationComponent

fun View.getApplicationComponent(): ApplicationComponent =
    context.getApplicationComponent()

fun AndroidViewModel.getApplicationComponent(): ApplicationComponent =
    getApplication<Application>().applicationComponent

fun ListenableWorker.getApplicationComponent(): ApplicationComponent =
    (applicationContext as Application).applicationComponent
