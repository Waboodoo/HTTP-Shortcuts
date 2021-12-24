package ch.rmy.android.framework.extensions

import androidx.lifecycle.LifecycleOwner
import ch.rmy.android.framework.utils.RxLifecycleObserver
import io.reactivex.Observable
import io.reactivex.functions.Consumer

fun <T : Any> Observable<T>.observe(
    lifecycleOwner: LifecycleOwner,
    onEvent: Consumer<T>,
): RxLifecycleObserver<T> =
    RxLifecycleObserver(this, onEvent).apply {
        lifecycleOwner.lifecycle.addObserver(this)
    }
