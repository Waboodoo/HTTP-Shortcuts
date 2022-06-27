package ch.rmy.android.http_shortcuts.test

import io.reactivex.Single

fun <T : Any> Single<T>.get(): T {
    val observer = test()
    val result = observer.values().first()
    observer.dispose()
    return result
}
