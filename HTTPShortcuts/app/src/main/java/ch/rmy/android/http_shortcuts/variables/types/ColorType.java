package ch.rmy.android.http_shortcuts.variables.types;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.ColorInt;

import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.OnColorSelectedListener;
import com.pavelsikun.vintagechroma.colormode.ColorMode;

import org.jdeferred.Deferred;

import ch.rmy.android.http_shortcuts.BaseActivity;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Variable;
import ch.rmy.android.http_shortcuts.variables.Showable;

class ColorType extends BaseVariableType implements AsyncVariableType {

    @Override
    public boolean hasTitle() {
        return false;
    }

    @Override
    public Showable createDialog(final Context context, final Controller controller, final Variable variable, final Deferred<String, Void, Void> deferredValue) {
        final ChromaDialog dialog = new ChromaDialog.Builder()
                .initialColor(getInitialColor(variable))
                .colorMode(ColorMode.RGB)
                .indicatorMode(IndicatorMode.HEX)
                .onColorSelected(new OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(@ColorInt int color) {
                        String colorFormatted = String.format("%06x", color & 0xffffff);
                        deferredValue.resolve(colorFormatted);
                        controller.setVariableValue(variable, colorFormatted);
                    }
                })
                .create();

        return new Showable() {
            @Override
            public void show() {
                dialog.show(((BaseActivity) context).getSupportFragmentManager(), "ColorDialog");

                // The following hack is needed because the ChromaDialog library does not have a method to register a dismiss listener
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if (deferredValue.isPending()) {
                                    deferredValue.reject(null);
                                }
                            }
                        });
                    }
                });
            }
        };
    }

    private int getInitialColor(Variable variable) {
        if (variable.isRememberValue() && variable.getValue().length() == 6) {
            try {
                int color = Integer.parseInt(variable.getValue(), 16);
                return color + 0xff000000;
            } catch (NumberFormatException e) {

            }
        }
        return Color.BLACK;
    }

    @Override
    protected VariableEditorFragment createEditorFragment() {
        return new TextEditorFragment();
    }

}
