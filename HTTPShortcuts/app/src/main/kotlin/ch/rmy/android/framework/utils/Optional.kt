package ch.rmy.android.framework.utils

class Optional<T : Any>(val value: T?) {
    companion object {
        fun <T : Any> empty() = Optional<T>(null)
    }
}
