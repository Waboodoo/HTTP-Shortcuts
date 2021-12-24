package ch.rmy.android.framework.data

import ch.rmy.android.framework.extensions.detachFromRealm
import io.realm.RealmList
import io.realm.RealmObject

class RealmListObservable<T : RealmObject>(
    realmFactory: RealmFactory,
    private val query: (RealmContext.() -> RealmList<T>),
) : RealmObservable<T>(realmFactory) {

    // Keeping a strong reference to the list to prevent garbage collection
    private var list: RealmList<T>? = null

    override fun registerChangeListener(realmContext: RealmContext, onDataChanged: (List<T>) -> Unit) {
        if (list != null) {
            throw IllegalStateException("RealmListObservable is already subscribed to.")
        }
        list = query.invoke(realmContext)
            .apply {
                processData(this, onDataChanged)
                addChangeListener { data ->
                    processData(data, onDataChanged)
                }
            }
    }

    private fun processData(data: RealmList<T>, onDataChanged: (List<T>) -> Unit) {
        data
            .takeIf { it.isValid && it.isLoaded }
            ?.detachFromRealm()
            ?.let(onDataChanged)
    }

    override fun onDispose() {
        list?.removeAllChangeListeners()
        list = null
    }
}
