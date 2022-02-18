package ch.rmy.android.framework.utils

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import io.reactivex.CompletableTransformer
import io.reactivex.Observable
import io.reactivex.SingleTransformer
import io.reactivex.subjects.BehaviorSubject
import kotlin.properties.Delegates

class ProgressMonitor {

    val anyInProgress: Observable<Boolean>
        get() = onChangedSubject

    private val onChangedSubject: BehaviorSubject<Boolean> =
        BehaviorSubject.createDefault(false)

    private var isInProgress: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            onChangedSubject.onNext(newValue)
        }
    }
    private val inProgress = mutableSetOf<String>()

    fun transformer(): CompletableTransformer =
        CompletableTransformer { upstream ->
            val uuid = newUUID()
            upstream
                .doOnSubscribe {
                    inProgress.add(uuid)
                    onChange()
                }
                .doOnTerminate {
                    inProgress.remove(uuid)
                    onChange()
                }
                .doOnDispose {
                    inProgress.remove(uuid)
                    onChange()
                }
        }

    fun <T> singleTransformer(): SingleTransformer<T, T> =
        SingleTransformer { upstream ->
            val uuid = newUUID()
            upstream
                .doOnSubscribe {
                    inProgress.add(uuid)
                    onChange()
                }
                .doOnEvent { _, _ ->
                    inProgress.remove(uuid)
                    onChange()
                }
                .doOnDispose {
                    inProgress.remove(uuid)
                    onChange()
                }
        }

    private fun onChange() {
        isInProgress = inProgress.isNotEmpty()
    }
}
