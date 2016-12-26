package ch.rmy.android.http_shortcuts.variables.types;

import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class TextType extends BaseVariableType implements AsyncVariableType {

    @Override
    public void setupDialog(final Controller controller, final Variable variable, MaterialDialog.Builder builder, final Deferred<String, Void, Void> deferredValue) {
        builder.input(null, variable.isRememberValue() ? variable.getValue() : "", new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                deferredValue.resolve(input.toString());
                controller.setVariableValue(variable, input.toString());
            }
        });
    }

    @Override
    protected VariableEditorFragment createEditorFragment() {
        return new TextEditorFragment();
    }
}
