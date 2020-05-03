package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.rmy.android.http_shortcuts.extensions.mapFor
import ch.rmy.android.http_shortcuts.extensions.replacePrefix
import ch.rmy.android.http_shortcuts.utils.IconUtil

class IconAdapter(private val context: Context, private val listener: (String) -> Unit) : RecyclerView.Adapter<IconViewHolder>() {

    private val normalIconNames: List<String>
        get() = Icons.ICONS
            .map {
                IconUtil.getIconName(context, it)
            }

    private val tintableIconNames: List<String>
        get() = Icons.TINTABLE_ICONS
            .map {
                IconUtil.getIconName(context, it)
            }

    private val icons = normalIconNames
        .mapFor(Icons.TintColors.values().asIterable()) { icons, tintColor ->
            icons.plus(tintableIconNames.map {
                it.replacePrefix(Icons.DEFAULT_TINT_PREFIX, tintColor.prefix)
            })
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(context, parent, listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(icons[position])
    }

    override fun getItemCount() = icons.size

}