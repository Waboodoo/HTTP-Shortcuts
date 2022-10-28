package ch.rmy.android.framework.extensions

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ch.rmy.android.framework.viewmodel.BaseViewModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified V : ViewModel> FragmentActivity.bindViewModel(): ReadOnlyProperty<FragmentActivity, V> =
    bindViewModelOf(V::class.java)

inline fun <reified V : ViewModel> Fragment.bindViewModel(noinline getKey: (() -> String)? = null): ReadOnlyProperty<Fragment, V> =
    bindViewModelOf(V::class.java, getKey)

fun <V : ViewModel> FragmentActivity.bindViewModelOf(clazz: Class<V>): ReadOnlyProperty<FragmentActivity, V> =
    bind(clazz, viewModelProviderFinder)

fun <V : ViewModel> Fragment.bindViewModelOf(clazz: Class<V>, getKey: (() -> String)? = null): ReadOnlyProperty<Fragment, V> =
    bind(clazz, viewModelProviderFinder, getKey)

@Suppress("unused")
private val FragmentActivity.viewModelProviderFinder: FragmentActivity.() -> ViewModelProvider
    get() = { ViewModelProvider(this) }

@Suppress("unused")
private val Fragment.viewModelProviderFinder: Fragment.() -> ViewModelProvider
    get() = { ViewModelProvider(this) }

private fun <T, V : ViewModel> bind(clazz: Class<V>, finder: T.() -> ViewModelProvider, getKey: (() -> String)? = null) =
    LazyWithTarget { t: T, _ ->
        t.finder().run {
            if (getKey != null) {
                get(getKey(), clazz)
            } else {
                get(clazz)
            }
        }
    }

private class LazyWithTarget<in T, out V : Any>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {

    private var value: V? = null

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == null) {
            value = initializer(thisRef, property)
        }
        return value!!
    }
}

val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

fun BaseViewModel<Unit, *>.initialize() {
    initialize(Unit)
}
