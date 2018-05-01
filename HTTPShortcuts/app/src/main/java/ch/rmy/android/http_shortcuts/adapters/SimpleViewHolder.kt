package ch.rmy.android.http_shortcuts.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

abstract class SimpleViewHolder<in T>(parent: ViewGroup, layoutRes: Int) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)) {

    abstract fun updateViews(item: T)

}