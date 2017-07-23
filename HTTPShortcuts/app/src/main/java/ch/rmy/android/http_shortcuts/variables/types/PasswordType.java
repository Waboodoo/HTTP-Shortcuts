package ch.rmy.android.http_shortcuts.variables.types;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.Showable;

public class PasswordType extends TextType {

    @Override
    public Showable createDialog(Context context, final Controller controller, final Variable variable, final Deferred<String, Void, Void> deferredValue) {
        final MaterialDialog.Builder builder = createDialogBuilder(context, variable, deferredValue);
        builder.input(null, variable.getRememberValue() ? variable.getValue() : "", new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                deferredValue.resolve(input.toString());
            }
        }).inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        return new Showable() {
            @Override
            public void show() {
                builder.show();
            }
        };
    }

}
