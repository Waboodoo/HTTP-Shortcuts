package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class IconAdapter(private val context: Context, private val listener: (String) -> Unit) : RecyclerView.Adapter<IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(context, parent, listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(Icons.ICONS[position])
    }

    override fun getItemCount() = Icons.ICONS.size

}