package ch.rmy.android.http_shortcuts.icons;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.ViewUtil;

public class IconView extends ImageView {

    private String iconName;

    public IconView(Context context) {
        super(context);
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        iconName = getContext().getResources().getResourceEntryName(resId);
        updateBackground();
    }

    private void updateBackground() {
        if (requiresBackground()) {
            setBackgroundResource(R.drawable.icon_background);
        } else {
            ViewUtil.clearBackground(this);
        }
    }

    private boolean requiresBackground() {
        return iconName != null && iconName.startsWith("white_");
    }

    public String getIconName() {
        return iconName;
    }

    public void setImageURI(Uri uri, String iconName) {
        setImageURI(uri);
        this.iconName = iconName;
        updateBackground();
    }

}
