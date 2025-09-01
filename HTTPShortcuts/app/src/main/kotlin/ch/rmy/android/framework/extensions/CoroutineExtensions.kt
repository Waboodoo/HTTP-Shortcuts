package ch.rmy.android.framework.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

suspend fun <T : Any> Flow<T?>.awaitNonNull(): T =
    first { it != null }!!
