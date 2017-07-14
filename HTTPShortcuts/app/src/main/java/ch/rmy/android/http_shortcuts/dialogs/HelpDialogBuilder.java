package ch.rmy.android.http_shortcuts.dialogs;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.utils.HTMLUtil;

public class HelpDialogBuilder {

    private final MaterialDialog.Builder builder;

    @Bind(R.id.help_text)
    TextView text;

    private View view;

    public HelpDialogBuilder(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.help_dialog, null);
        ButterKnife.bind(this, view);

        builder = new MaterialDialog.Builder(context)
                .customView(view, false)
                .positiveText(android.R.string.ok);
    }

    public HelpDialogBuilder title(@StringRes int title) {
        builder.title(title);
        return this;
    }

    public HelpDialogBuilder message(@StringRes int message) {
        text.setText(HTMLUtil.getHTML(view.getContext(), message));
        return this;
    }

    public HelpDialog build() {
        return new HelpDialog(builder.build());
    }

}
