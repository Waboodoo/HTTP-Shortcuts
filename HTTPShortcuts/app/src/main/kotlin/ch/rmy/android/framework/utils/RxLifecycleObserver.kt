package ch.rmy.android.framework.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer

class RxLifecycleObserver<T>(
    private val observable: Observable<T>,
    private val onEvent: Consumer<T>,
    private val onError: Consumer<Throwable>? = null,
) : DefaultLifecycleObserver {

    private var disposable: Disposable? = null

    override fun onStart(owner: LifecycleOwner) {
        disposable = observable
            .observeOn(AndroidSchedulers.mainThread())
            .let {
                if (onError != null) {
                    it.subscribe(onEvent, onError)
                } else {
                    it.subscribe(onEvent)
                }
            }
    }

    override fun onStop(owner: LifecycleOwner) {
        disposable?.dispose()
    }
}
