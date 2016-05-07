package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.http.HttpRequester;
import ch.rmy.android.http_shortcuts.listeners.OnShortcutClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.Settings;

public class ListFragment extends Fragment implements OnShortcutClickedListener {

    private final static int REQUEST_CREATE_SHORTCUT = 1;
    private final static int REQUEST_EDIT_SHORTCUT = 2;

    public static final String ARGUMENT_CATEGORY_ID = "category_id";
    public static final String ARGUMENT_PLACEMENT_MODE = "placement_mode";

    @Bind(R.id.shortcut_list)
    RecyclerView shortcutList;

    private boolean shortcutPlacementMode;

    private Controller controller;
    private Category category;
    private ShortcutAdapter adapter;

    public static ListFragment newInstance(Category category, boolean shortcutPlacementMode) {
        ListFragment fragment = new ListFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_CATEGORY_ID, category.getId());
        arguments.putBoolean(ARGUMENT_PLACEMENT_MODE, shortcutPlacementMode);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long categoryId = getArguments().getLong(ARGUMENT_CATEGORY_ID);
        shortcutPlacementMode = getArguments().getBoolean(ARGUMENT_PLACEMENT_MODE);

        controller = new Controller(getContext());

        category = controller.getCategoryById(categoryId);
        adapter = new ShortcutAdapter(getContext(), category);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.destroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, parent, false);
        ButterKnife.bind(this, view);

        adapter.setOnShortcutClickListener(this);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        shortcutList.setLayoutManager(manager);
        shortcutList.setHasFixedSize(true);
        shortcutList.addItemDecoration(new ShortcutListDecorator(getContext(), R.drawable.list_divider));
        shortcutList.setAdapter(adapter);

        return view;
    }


    @Override
    public void onShortcutClicked(Shortcut shortcut, View view) {
        if (shortcutPlacementMode) {
            getTabHost().returnForHomeScreen(shortcut);
        } else {
            String action = new Settings(getContext()).getClickBehavior();
            switch (action) {
                case Settings.CLICK_BEHAVIOR_RUN:
                    executeShortcut(shortcut);
                    break;
                case Settings.CLICK_BEHAVIOR_EDIT:
                    editShortcut(shortcut);
                    break;
                case Settings.CLICK_BEHAVIOR_MENU:
                    showContextMenu(shortcut);
                    break;
            }
        }
    }

    @Override
    public void onShortcutLongClicked(Shortcut shortcut, View view) {
        showContextMenu(shortcut);
    }

    private void showContextMenu(final Shortcut shortcut) {
        (new MaterialDialog.Builder(getContext()))
                .title(shortcut.getName())
                .items(R.array.context_menu_items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        performContextMenuAction(which, shortcut);
                    }
                })
                .show();
    }

    private void performContextMenuAction(int action, Shortcut shortcut) {
        switch (action) {
            case 0:
                getTabHost().placeShortcutOnHomeScreen(shortcut);
                return;
            case 1:
                executeShortcut(shortcut);
                return;
            case 2:
                editShortcut(shortcut);
                return;
            case 3:
                moveShortcut(shortcut, -1);
                return;
            case 4:
                moveShortcut(shortcut, 1);
                return;
            case 5:
                duplicateShortcut(shortcut);
                return;
            case 6:
                showDeleteDialog(shortcut);
                return;
        }

        return;
    }


    private void executeShortcut(Shortcut shortcut) {
        HttpRequester.executeShortcut(getContext(), shortcut.getId(), controller);
    }

    private void editShortcut(Shortcut shortcut) {
        Intent intent = new Intent(getContext(), EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcut.getId());
        startActivityForResult(intent, REQUEST_EDIT_SHORTCUT);
    }

    private void moveShortcut(Shortcut shortcut, int offset) {
        int position = category.getShortcuts().indexOf(shortcut) + offset;
        if (position < 0 || position > category.getShortcuts().size()) {
            return;
        }
        if (position == category.getShortcuts().size()) {
            controller.moveShortcut(shortcut, category);
        } else {
            controller.moveShortcut(shortcut, position);
        }
    }

    private void duplicateShortcut(Shortcut shortcut) {
        String newName = String.format(getText(R.string.copy).toString(), shortcut.getName());
        Shortcut duplicate = controller.persist(shortcut.duplicate(newName));
        controller.moveShortcut(duplicate, category);
        int position = category.getShortcuts().indexOf(shortcut);
        controller.moveShortcut(duplicate, position + 1);

        getTabHost().showSnackbar(String.format(getText(R.string.shortcut_duplicated).toString(), shortcut.getName()));
    }

    private void showDeleteDialog(final Shortcut shortcut) {
        (new MaterialDialog.Builder(getContext()))
                .content(R.string.confirm_delete_message)
                .positiveText(R.string.dialog_delete)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteShortcut(shortcut);
                    }
                })
                .negativeText(R.string.dialog_cancel)
                .show();
    }

    private void deleteShortcut(Shortcut shortcut) {
        getTabHost().showSnackbar(String.format(getText(R.string.shortcut_deleted).toString(), shortcut.getName()));
        controller.deleteShortcut(shortcut);
    }

    public void openEditorForCreation() {
        Intent intent = new Intent(getContext(), EditorActivity.class);
        startActivityForResult(intent, REQUEST_CREATE_SHORTCUT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CREATE_SHORTCUT) {
            long shortcutId = intent.getLongExtra(EditorActivity.EXTRA_SHORTCUT_ID, 0);
            Shortcut shortcut = controller.getShortcutById(shortcutId);
            if (shortcut == null) {
                return;
            }
            controller.moveShortcut(shortcut, category);

            if (shortcutPlacementMode) {
                getTabHost().returnForHomeScreen(shortcut);
            }
        }
    }

    private TabHost getTabHost() {
        return (TabHost) getActivity();
    }

    public interface TabHost {

        void returnForHomeScreen(Shortcut shortcut);

        void placeShortcutOnHomeScreen(Shortcut shortcut);

        void showSnackbar(CharSequence message);

    }

}
