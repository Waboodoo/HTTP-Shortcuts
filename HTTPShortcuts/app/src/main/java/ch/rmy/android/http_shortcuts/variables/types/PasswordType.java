package ch.rmy.android.http_shortcuts.variables.types;

import android.support.annotation.NonNull;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class PasswordType extends TextType {

    @Override
    public void setupDialog(Controller controller, Variable variable, MaterialDialog.Builder builder, final Deferred<String, Void, Void> deferredValue) {
        builder.input(null, variable.getValue(), new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                deferredValue.resolve(input.toString());
            }
        }).inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
}
