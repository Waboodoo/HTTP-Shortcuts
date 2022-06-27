package ch.rmy.android.http_shortcuts.dagger

import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.work.ListenableWorker

fun Context.getApplicationComponent(): ApplicationComponent =
    (applicationContext as ApplicationComponentProvider).applicationComponent

fun View.getApplicationComponent(): ApplicationComponent =
    context.getApplicationComponent()

fun AndroidViewModel.getApplicationComponent(): ApplicationComponent =
    (getApplication<Application>() as ApplicationComponentProvider).applicationComponent

fun ListenableWorker.getApplicationComponent(): ApplicationComponent =
    (applicationContext as ApplicationComponentProvider).applicationComponent
