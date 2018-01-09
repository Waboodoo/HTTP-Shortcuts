package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class IconAdapter(private val context: Context, private val listener: (String) -> Unit) : RecyclerView.Adapter<IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
            IconViewHolder(context, parent, listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(Icons.ICONS[position])
    }

    override fun getItemCount() = Icons.ICONS.size

}