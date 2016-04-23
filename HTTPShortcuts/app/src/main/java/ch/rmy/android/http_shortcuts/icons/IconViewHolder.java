package ch.rmy.android.http_shortcuts.icons;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;

public class IconViewHolder extends RecyclerView.ViewHolder {

    private final IconView iconView;

    public IconViewHolder(Context context, ViewGroup parent, final OnIconSelectedListener listener) {
        super(LayoutInflater.from(context).inflate(R.layout.icon_list_item, parent, false));

        iconView = (IconView) itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconSelected(iconView.getIconName());
            }
        });
    }

    public void setIcon(int iconResource) {
        iconView.setImageResource(iconResource);
    }

}
