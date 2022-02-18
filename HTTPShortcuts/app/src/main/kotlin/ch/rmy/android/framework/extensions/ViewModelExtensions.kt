package ch.rmy.android.framework.extensions

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ch.rmy.android.framework.viewmodel.BaseViewModel
import kotlin.properties.ReadOnlyProperty

inline fun <reified V : ViewModel> FragmentActivity.bindViewModel(): ReadOnlyProperty<FragmentActivity, V> =
    bindViewModelOf(V::class.java)

inline fun <reified V : ViewModel> Fragment.bindViewModel(noinline getKey: (() -> String)? = null): ReadOnlyProperty<Fragment, V> =
    bindViewModelOf(V::class.java, getKey)

fun <V : ViewModel> FragmentActivity.bindViewModelOf(clazz: Class<V>): ReadOnlyProperty<FragmentActivity, V> =
    bind(clazz, viewModelProviderFinder)

fun <V : ViewModel> Fragment.bindViewModelOf(clazz: Class<V>, getKey: (() -> String)? = null): ReadOnlyProperty<Fragment, V> =
    bind(clazz, viewModelProviderFinder, getKey)

private val FragmentActivity.viewModelProviderFinder: FragmentActivity.() -> ViewModelProvider
    get() = { ViewModelProviders.of(this) }

private val Fragment.viewModelProviderFinder: Fragment.() -> ViewModelProvider
    get() = { ViewModelProviders.of(activity!!) }

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

@Deprecated("Avoid using context in a view model")
val AndroidViewModel.context: Context
    get() = getApplication<Application>().applicationContext

fun BaseViewModel<Unit, *>.initialize() {
    initialize(Unit)
}
