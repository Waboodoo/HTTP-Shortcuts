package ch.rmy.android.framework.extensions

import androidx.lifecycle.LifecycleOwner
import ch.rmy.android.framework.utils.Optional
import ch.rmy.android.framework.utils.RxLifecycleObserver
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

fun <T : Any> Observable<T>.observe(
    lifecycleOwner: LifecycleOwner,
    onEvent: Consumer<T>,
): RxLifecycleObserver<T> =
    RxLifecycleObserver(this, onEvent).apply {
        lifecycleOwner.lifecycle.addObserver(this)
    }

fun <T : Any> Single<Optional<T>>.subscribeOptional(
    onSuccess: (T?) -> Unit,
    onError: ((Throwable) -> Unit),
): Disposable =
    subscribe(
        { optional ->
            onSuccess(optional.value)
        },
        onError,
    )

fun <T : Any> Single<Optional<T>>.subscribeOptional(
    onSuccess: (T?) -> Unit,
): Disposable =
    subscribe { optional ->
        onSuccess(optional.value)
    }
