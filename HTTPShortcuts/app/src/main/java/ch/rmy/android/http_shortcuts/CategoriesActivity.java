package ch.rmy.android.http_shortcuts;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.Bind;
import ch.rmy.android.http_shortcuts.listeners.OnItemClickedListener;
import ch.rmy.android.http_shortcuts.realm.Controller;
import ch.rmy.android.http_shortcuts.realm.models.Category;

public class CategoriesActivity extends BaseActivity {

    @Bind(R.id.category_list)
    RecyclerView categoryList;

    private Controller controller;
    private CategoryAdapter adapter;

    private OnItemClickedListener<Category> clickedListener = new OnItemClickedListener<Category>() {
        @Override
        public void onItemClicked(Category category) {
            showContextMenu(category);
        }

        @Override
        public void onItemLongClicked(Category category) {
            showContextMenu(category);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        controller = destroyer.own(new Controller(this));
        adapter = destroyer.own(new CategoryAdapter(this));
        adapter.setParent(controller.getBase());

        LinearLayoutManager manager = new LinearLayoutManager(this);
        categoryList.setLayoutManager(manager);
        categoryList.setHasFixedSize(true);
        categoryList.addItemDecoration(new ShortcutListDecorator(this, R.drawable.list_divider));
        categoryList.setAdapter(adapter);

        adapter.setOnItemClickListener(clickedListener);
    }

    private void showContextMenu(final Category category) {
        (new MaterialDialog.Builder(this))
                .title(category.getName())
                .items(R.array.category_menu_items)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        performContextMenuAction(which, category);
                    }
                })
                .show();
    }

    private void performContextMenuAction(int action, Category category) {
        switch (action) {
            case 0:
                showRenameDialog(category);
                return;
            case 1:
                return;
            case 2:
                return;
            case 3:
                showDeleteDialog(category);
                return;
        }
    }

    private void showRenameDialog(final Category category) {
        new MaterialDialog.Builder(this)
                .title(R.string.title_rename_category)
                .inputRange(2, 20)
                .input(null, category.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        renameCategory(category, input.toString());
                    }
                }).show();
    }

    private void renameCategory(Category category, String newName) {
        controller.renameCategory(category, newName);
        showSnackbar(R.string.message_category_renamed);
    }

    private void showDeleteDialog(final Category category) {
        (new MaterialDialog.Builder(this))
                .content(R.string.confirm_delete_category_message)
                .positiveText(R.string.dialog_delete)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteCategory(category);
                    }
                })
                .negativeText(R.string.dialog_cancel)
                .show();
    }

    private void deleteCategory(Category category) {
        //TODO
        //getTabHost().showSnackbar(String.format(getText(R.string.shortcut_deleted).toString(), shortcut.getName()));
        //controller.deleteShortcut(shortcut);
    }

}
