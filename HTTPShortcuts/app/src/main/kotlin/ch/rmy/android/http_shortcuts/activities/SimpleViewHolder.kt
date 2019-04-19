package ch.rmy.android.http_shortcuts.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleViewHolder<in T>(parent: ViewGroup, layoutRes: Int) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {

    abstract fun updateViews(item: T)

}