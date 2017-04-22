package ch.rmy.android.http_shortcuts.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ShortcutListDecorator extends RecyclerView.ItemDecoration {

    private Drawable divider;

    public ShortcutListDecorator(Context context, int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int paddingLeft = parent.getPaddingLeft();
        int paddingRight = parent.getWidth() - parent.getPaddingRight();

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int paddingTop = child.getBottom() + params.bottomMargin;
            int paddingBottom = paddingTop + divider.getIntrinsicHeight();
            divider.setBounds(paddingLeft, paddingTop, paddingRight, paddingBottom);
            divider.draw(canvas);
        }
    }
}