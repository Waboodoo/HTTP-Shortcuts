package ch.rmy.android.http_shortcuts.variables.types;

import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import java.util.ArrayList;
import java.util.List;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Option;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class SelectType extends BaseVariableType implements AsyncVariableType {

    @Override
    public void setupDialog(final Controller controller, final Variable variable, MaterialDialog.Builder builder, final Deferred<String, Void, Void> deferredValue) {
        List<CharSequence> items = new ArrayList<>();
        for (Option option : variable.getOptions()) {
            items.add(option.getLabel());
        }

        builder.items()
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        String value = variable.getOptions().get(which).getValue();
                        deferredValue.resolve(value);
                        controller.setVariableValue(variable, value);
                    }
                });
    }
}
