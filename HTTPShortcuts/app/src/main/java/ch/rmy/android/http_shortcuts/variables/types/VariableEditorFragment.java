package ch.rmy.android.http_shortcuts.variables.types;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.R;
import ch.rmy.android.http_shortcuts.VariableEditorActivity;
import ch.rmy.android.http_shortcuts.realm.models.Variable;

public class VariableEditorFragment extends Fragment {

    protected int getLayoutResource() {
        return R.layout.empty_layout;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), parent, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((VariableEditorActivity) getActivity()).onFragmentStarted();
    }

    public void updateViews(Variable variable) {

    }

    public void compileIntoVariable(Variable variable) {

    }

}
