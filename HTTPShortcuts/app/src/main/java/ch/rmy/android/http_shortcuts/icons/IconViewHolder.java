package ch.rmy.android.http_shortcuts.icons;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;

public class IconViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final ImageView iconView;
    private String resourceName;

    public IconViewHolder(Context context, ViewGroup parent, final OnIconSelectedListener listener) {
        super(LayoutInflater.from(context).inflate(R.layout.icon_list_item, parent, false));
        this.context = context;

        iconView = (ImageView) itemView;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onIconSelected(resourceName);
            }
        });
    }

    public void setIcon(int iconResource) {
        iconView.setImageResource(iconResource);
        resourceName = findResourceName(iconResource);

        iconView.setBackgroundColor(resourceName.startsWith("white_") ? Color.BLACK : Color.TRANSPARENT);
    }

    private String findResourceName(int iconResource) {
        return context.getResources().getResourceEntryName(iconResource);
    }
}
