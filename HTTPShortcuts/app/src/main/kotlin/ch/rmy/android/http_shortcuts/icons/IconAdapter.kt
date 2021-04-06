package ch.rmy.android.http_shortcuts.icons

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class IconAdapter(private val context: Context, private val listener: (ShortcutIcon.BuiltInIcon) -> Unit) : RecyclerView.Adapter<IconViewHolder>() {

    private val normalIcons: List<ShortcutIcon.BuiltInIcon> =
        Icons.ICONS
            .map {
                ShortcutIcon.BuiltInIcon.fromDrawableResource(context, it)
            }

    private val tintedIcons: List<ShortcutIcon.BuiltInIcon> =
        Icons.TintColors.values()
            .flatMap { tint ->
                Icons.TINTABLE_ICONS.map { iconResource ->
                    ShortcutIcon.BuiltInIcon.fromDrawableResource(context, iconResource, tint)
                }
            }

    private val icons = normalIcons.plus(tintedIcons)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder =
        IconViewHolder(context, parent, listener)

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.setIcon(icons[position])
    }

    override fun getItemCount() = icons.size

}