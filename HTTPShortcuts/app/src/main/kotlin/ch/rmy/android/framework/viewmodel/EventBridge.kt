package ch.rmy.android.framework.viewmodel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class EventBridge<T : Any>(private val clazz: Class<T>) {

    fun submit(event: T) {
        getChannel(clazz).trySend(event)
    }

    val events: Flow<T> =
        getChannel(clazz).receiveAsFlow()

    @Suppress("UNCHECKED_CAST")
    companion object {

        private val channelsMapSingleton = mutableMapOf<Class<*>, Channel<*>>()

        private fun <T> getChannel(clazz: Class<T>): Channel<T> =
            channelsMapSingleton.getOrPut(clazz) { Channel<T>(capacity = Channel.UNLIMITED) } as Channel<T>
    }
}
