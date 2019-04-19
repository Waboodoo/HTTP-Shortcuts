package ch.rmy.android.http_shortcuts.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.data.livedata.RealmSingleLiveData
import ch.rmy.android.http_shortcuts.data.models.HasId
import io.realm.RealmObject

abstract class BaseViewHolder<in T>(parent: ViewGroup, layoutRes: Int, baseAdapter: BaseAdapter<T>) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) where T : RealmObject, T : HasId {

    private var item: T? = null

    init {
        itemView.setOnClickListener {
            item?.let {
                baseAdapter.clickListener?.invoke(RealmSingleLiveData(it))
            }
        }
        itemView.setOnLongClickListener {
            item?.let {
                baseAdapter.longClickListener?.invoke(RealmSingleLiveData(it)) == true
            } ?: false
        }
    }

    fun setItem(item: T) {
        this.item = item
        updateViews(item)
    }

    protected abstract fun updateViews(item: T)

}