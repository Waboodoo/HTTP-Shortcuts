package ch.rmy.android.http_shortcuts.dialogs;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.http.ShortcutResponse;

public class CurlExportDialog {

    private final Context context;
    private final String title;
    private final String curlCommand;

    @Bind(R.id.curl_export_textview)
    TextView text;

    public CurlExportDialog(Context context, String title, String curlCommand) {
        this.context = context;
        this.title = title;
        this.curlCommand = curlCommand;
    }

    public void show() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.curl_export_dialog, null);
        ButterKnife.bind(this, view);

        new MaterialDialog.Builder(context)
                .title(title)
                .customView(view, false)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.share_button)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        shareCurlExport();
                    }
                })
                .show();

        text.setText(curlCommand);
    }

    private void shareCurlExport() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType(ShortcutResponse.TYPE_TEXT);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, curlCommand);
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_title)));
    }

}
