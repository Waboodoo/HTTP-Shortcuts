package ch.rmy.android.http_shortcuts.variables.types;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import java.util.ArrayList;
import java.util.List;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Option;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.Showable;

class SelectType extends BaseVariableType implements AsyncVariableType {

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public Showable createDialog(Context context, final Controller controller, final Variable variable, final Deferred<String, Void, Void> deferredValue) {
        List<CharSequence> items = new ArrayList<>();
        for (Option option : variable.getOptions()) {
            items.add(option.getLabel());
        }

        final MaterialDialog.Builder builder = createDialogBuilder(context, variable, deferredValue);
        builder.items(items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        String value = variable.getOptions().get(which).getValue();
                        deferredValue.resolve(value);
                        controller.setVariableValue(variable, value);
                    }
                });
        return new Showable() {
            @Override
            public void show() {
                builder.show();
            }
        };
    }

    @Override
    protected SelectEditorFragment createEditorFragment() {
        return new SelectEditorFragment();
    }

}
