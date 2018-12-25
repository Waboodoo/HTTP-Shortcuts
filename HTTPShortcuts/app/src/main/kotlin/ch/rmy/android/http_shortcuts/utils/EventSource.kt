package ch.rmy.android.http_shortcuts.utils

class EventSource<T> {

    private val observers = mutableListOf<(T) -> Unit>()

    fun add(observer: (T) -> Unit): Destroyable {
        observers.add(observer)
        return object : Destroyable {
            override fun destroy() {
                observers.remove(observer)
            }
        }
    }

    fun notifyObservers(item: T) {
        observers.forEach { observer ->
            observer.invoke(item)
        }
    }

}