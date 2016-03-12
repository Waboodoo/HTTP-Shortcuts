package ch.rmy.android.http_shortcuts.icons;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import ch.rmy.android.http_shortcuts.listeners.OnIconSelectedListener;

class IconAdapter extends RecyclerView.Adapter<IconViewHolder> {

    private final Context context;
    private final OnIconSelectedListener listener;

    protected IconAdapter(Context context, OnIconSelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IconViewHolder(context, parent, listener);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.setIcon(Icons.ICONS[position]);
    }

    @Override
    public int getItemCount() {
        return Icons.ICONS.length;
    }
}