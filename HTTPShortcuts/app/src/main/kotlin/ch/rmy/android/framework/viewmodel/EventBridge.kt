package ch.rmy.android.framework.viewmodel

import com.victorrendina.rxqueue2.QueueSubject
import io.reactivex.Observable

class EventBridge<T : Any>(private val clazz: Class<T>) {

    fun submit(event: T) {
        getSubject(clazz).onNext(event)
    }

    val events: Observable<T> =
        getSubject(clazz)

    companion object {

        private val subjectsMapSingleton = mutableMapOf<Class<*>, QueueSubject<*>>()

        private fun <T> getSubject(clazz: Class<T>): QueueSubject<T> =
            subjectsMapSingleton.getOrPut(clazz) { QueueSubject.create<T>() } as QueueSubject<T>
    }
}
