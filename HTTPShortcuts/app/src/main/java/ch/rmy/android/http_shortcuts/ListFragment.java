package ch.rmy.android.http_shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ch.rmy.android.http_shortcuts.adapters.ShortcutAdapter;
import ch.rmy.android.http_shortcuts.adapters.ShortcutGridAdapter;
import ch.rmy.android.http_shortcuts.adapters.ShortcutListAdapter;
import ch.rmy.android.http_shortcuts.dialogs.CurlExportDialog;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;
import ch.rmy.android.http_shortcuts.realm.models.Header;
import ch.rmy.android.http_shortcuts.realm.models.Parameter;
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution;
import ch.rmy.android.http_shortcuts.realm.models.Shortcut;
import ch.rmy.android.http_shortcuts.utils.IntentUtil;
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager;
import ch.rmy.android.http_shortcuts.utils.MenuDialogBuilder;
import ch.rmy.android.http_shortcuts.utils.SelectionMode;
import ch.rmy.android.http_shortcuts.utils.Settings;
import ch.rmy.android.http_shortcuts.utils.ShortcutListDecorator;
import ch.rmy.curlcommand.CurlCommand;
import ch.rmy.curlcommand.CurlConstructor;

public class ListFragment extends Fragment {

    private final static int REQUEST_EDIT_SHORTCUT = 2;

    @Bind(R.id.shortcut_list)
    RecyclerView shortcutList;

    private long categoryId;
    private SelectionMode selectionMode;
    @Nullable
    private Category category;

    private Controller controller;
    private List<Category> categories;

    private String layoutType;
    private RecyclerView.ItemDecoration listDivider;

    private final OnItemClickedListener<Shortcut> clickListener = new OnItemClickedListener<Shortcut>() {
        @Override
        public void onItemClicked(Shortcut shortcut) {
            switch (selectionMode) {
                case HOME_SCREEN:
                case PLUGIN:
                    getTabHost().selectShortcut(shortcut);
                    break;
                default:
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
        public void onItemLongClicked(Shortcut shortcut) {
            showContextMenu(shortcut);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controller = new Controller();
        categories = controller.getCategories();

        listDivider = new ShortcutListDecorator(getContext(), R.drawable.list_divider);
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
        onCategoryChanged();
    }

    private void onCategoryChanged() {
        if (controller == null) {
            return;
        }
        category = controller.getCategoryById(categoryId);

        if (category != null && !category.getLayoutType().equals(layoutType)) {
            layoutType = category.getLayoutType();
            onLayoutTypeChanged();
        }
    }

    private void onLayoutTypeChanged() {
        RecyclerView.LayoutManager manager;
        ShortcutAdapter adapter;
        switch (layoutType) {
            case Category.LAYOUT_GRID: {
                adapter = new ShortcutGridAdapter(getContext());
                manager = new GridLayoutManager(getContext(), getNumberOfColumns());
                shortcutList.removeItemDecoration(listDivider);
                break;
            }
            case Category.LAYOUT_LINEAR_LIST:
            default: {
                adapter = new ShortcutListAdapter(getContext());
                manager = new LinearLayoutManager(getContext());
                shortcutList.addItemDecoration(listDivider);
                break;
            }
        }
        adapter.setParent(category);
        adapter.setPendingShortcuts(controller.getShortcutsPendingExecution());
        adapter.setOnItemClickListener(clickListener);

        shortcutList.setLayoutManager(manager);
        shortcutList.setAdapter(adapter);
    }

    public int getNumberOfColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / 90);
    }

    public long getCategoryId() {
        return categoryId;
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
        shortcutList.setHasFixedSize(true);
        onCategoryChanged();
        return view;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;
    }

    private void showContextMenu(final Shortcut shortcut) {
        MenuDialogBuilder builder = new MenuDialogBuilder(getContext())
                .title(shortcut.getName());

        builder.item(R.string.action_place, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                getTabHost().placeShortcutOnHomeScreen(shortcut);
            }
        });
        builder.item(R.string.action_run, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                executeShortcut(shortcut);
            }
        });
        builder.item(R.string.action_edit, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                editShortcut(shortcut);
            }
        });
        if (canMoveShortcut(shortcut)) {
            builder.item(R.string.action_move, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    openMoveDialog(shortcut);
                }
            });
        }
        builder.item(R.string.action_duplicate, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                duplicateShortcut(shortcut);
            }
        });
        if (getPendingExecution(shortcut) != null) {
            builder.item(R.string.action_cancel_pending, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    cancelPendingExecution(shortcut);
                }
            });
        }
        builder.item(R.string.action_curl_export, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                showCurlExportDialog(shortcut);
            }
        });
        builder.item(R.string.action_delete, new MenuDialogBuilder.Action() {
            @Override
            public void execute() {
                showDeleteDialog(shortcut);
            }
        });
        builder.show();
    }

    private PendingExecution getPendingExecution(Shortcut shortcut) {
        for (PendingExecution pendingExecution : controller.getShortcutsPendingExecution()) {
            if (pendingExecution.getShortcutId() == shortcut.getId()) {
                return pendingExecution;
            }
        }
        return null;
    }

    private void executeShortcut(Shortcut shortcut) {
        Intent intent = IntentUtil.createIntent(getContext(), shortcut.getId());
        startActivity(intent);
    }

    private void editShortcut(Shortcut shortcut) {
        Intent intent = new Intent(getContext(), EditorActivity.class);
        intent.putExtra(EditorActivity.EXTRA_SHORTCUT_ID, shortcut.getId());
        startActivityForResult(intent, REQUEST_EDIT_SHORTCUT);
    }

    private boolean canMoveShortcut(Shortcut shortcut) {
        return canMoveShortcut(shortcut, -1) || canMoveShortcut(shortcut, +1) || categories.size() > 1;
    }

    private boolean canMoveShortcut(Shortcut shortcut, int offset) {
        int position = category.getShortcuts().indexOf(shortcut) + offset;
        return position >= 0 && position < category.getShortcuts().size();
    }

    private void openMoveDialog(final Shortcut shortcut) {
        MenuDialogBuilder builder = new MenuDialogBuilder(getContext());
        if (canMoveShortcut(shortcut, -1)) {
            builder.item(R.string.action_move_up, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    moveShortcut(shortcut, -1);
                }
            });
        }
        if (canMoveShortcut(shortcut, 1)) {
            builder.item(R.string.action_move_down, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    moveShortcut(shortcut, 1);
                }
            });
        }
        if (categories.size() > 1) {
            builder.item(R.string.action_move_to_category, new MenuDialogBuilder.Action() {
                @Override
                public void execute() {
                    showMoveToCategoryDialog(shortcut);
                }
            });
        }
        builder.show();
    }

    private void moveShortcut(Shortcut shortcut, int offset) {
        if (!canMoveShortcut(shortcut, offset)) {
            return;
        }
        int position = category.getShortcuts().indexOf(shortcut) + offset;
        if (position == category.getShortcuts().size()) {
            controller.moveShortcut(shortcut, category);
        } else {
            controller.moveShortcut(shortcut, position);
        }
        LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());
    }

    private void showMoveToCategoryDialog(final Shortcut shortcut) {
        List<CharSequence> categoryNames = new ArrayList<>();
        final List<Category> categories = new ArrayList<>();
        for (Category category : this.categories) {
            if (category.getId() != this.category.getId()) {
                categoryNames.add(category.getName());
                categories.add(category);
            }
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.title_move_to_category)
                .items(categoryNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        if (which < categories.size() && categories.get(which).isValid()) {
                            moveShortcut(shortcut, categories.get(which));
                        }
                    }
                })
                .show();
    }

    private void moveShortcut(Shortcut shortcut, Category category) {
        controller.moveShortcut(shortcut, category);
        getTabHost().showSnackbar(String.format(getString(R.string.shortcut_moved), shortcut.getName()));
        LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());
    }

    private void duplicateShortcut(Shortcut shortcut) {
        String newName = String.format(getString(R.string.copy), shortcut.getName());
        Shortcut duplicate = controller.persist(shortcut.duplicate(newName));
        controller.moveShortcut(duplicate, category);
        int position = category.getShortcuts().indexOf(shortcut);
        controller.moveShortcut(duplicate, position + 1);
        LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());

        getTabHost().showSnackbar(String.format(getString(R.string.shortcut_duplicated), shortcut.getName()));
    }

    private void cancelPendingExecution(Shortcut shortcut) {
        PendingExecution pendingExecution = getPendingExecution(shortcut);
        if (pendingExecution == null) {
            return;
        }
        controller.removePendingExecution(pendingExecution);
        getTabHost().showSnackbar(String.format(getString(R.string.pending_shortcut_execution_cancelled), shortcut.getName()));
    }

    private void showCurlExportDialog(final Shortcut shortcut) {
        CurlCommand.Builder builder = new CurlCommand.Builder()
                .url(shortcut.getUrl())
                .username(shortcut.getUsername())
                .password(shortcut.getPassword())
                .method(shortcut.getMethod())
                .timeout(shortcut.getTimeout());

        for (Header header : shortcut.getHeaders()) {
            builder.header(header.getKey(), header.getValue());
        }

        for (Parameter parameter : shortcut.getParameters()) {
            try {
                builder.data(URLEncoder.encode(parameter.getKey(), "UTF-8")
                        + "="
                        + URLEncoder.encode(parameter.getValue(), "UTF-8")
                        + "&"
                );
            } catch (UnsupportedEncodingException e) {

            }
        }

        builder.data(shortcut.getBodyContent());

        new CurlExportDialog(
                getContext(),
                shortcut.getSafeName(getContext()),
                CurlConstructor.toCurlCommandString(builder.build())
        ).show();
    }

    private void showDeleteDialog(final Shortcut shortcut) {
        (new MaterialDialog.Builder(getContext()))
                .content(R.string.confirm_delete_shortcut_message)
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
        getTabHost().showSnackbar(String.format(getString(R.string.shortcut_deleted), shortcut.getName()));
        getTabHost().removeShortcutFromHomeScreen(shortcut);
        controller.deleteShortcut(shortcut);
        LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());
    }

    private TabHost getTabHost() {
        return (TabHost) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_EDIT_SHORTCUT) {
            LauncherShortcutManager.updateAppShortcuts(getContext(), controller.getCategories());
        }
    }

    interface TabHost {

        void selectShortcut(Shortcut shortcut);

        void placeShortcutOnHomeScreen(Shortcut shortcut);

        void removeShortcutFromHomeScreen(Shortcut shortcut);

        void showSnackbar(CharSequence message);

    }

}
