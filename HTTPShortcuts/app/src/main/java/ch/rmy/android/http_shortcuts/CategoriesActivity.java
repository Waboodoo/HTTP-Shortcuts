package ch.rmy.android.http_shortcuts;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
        public void onItemClicked(Category item) {

        }

        @Override
        public void onItemLongClicked(Category item) {

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
}
