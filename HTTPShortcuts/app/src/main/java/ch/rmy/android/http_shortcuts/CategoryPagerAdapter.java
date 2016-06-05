package ch.rmy.android.http_shortcuts;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class CategoryPagerAdapter extends FragmentPagerAdapter {

    private final List<ListFragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();

    public CategoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(ListFragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);
    }

    @Override
    public ListFragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}