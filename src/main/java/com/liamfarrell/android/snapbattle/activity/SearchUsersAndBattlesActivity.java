package com.liamfarrell.android.snapbattle.activity;


/**
 * Created by Liam on 30/07/2017.
 */


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.liamfarrell.android.snapbattle.R;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersAndBattlesActivity extends AppCompatActivity {

    private SearchView mSearchView;
    private BattleNameSearchFragment mBattleNameSearchFragment;
    private UserSearchFragment mUserSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_users_battles);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);




    }

    private void initializeFragments()
    {
        mBattleNameSearchFragment = new BattleNameSearchFragment();
        mUserSearchFragment = new UserSearchFragment();

        ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchBar = menu.findItem(R.id.action_search);
        mSearchView = (SearchView)searchBar.getActionView();
        mSearchView.onActionViewExpanded();
        initializeFragments();
        return super.onCreateOptionsMenu(menu);
    }


    public SearchView getSearchView()
    {
        return mSearchView;
    }

    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mBattleNameSearchFragment, getResources().getText(R.string.battles_search_tab_title).toString());
        mBattleNameSearchFragment.setQueryChangeListener(mSearchView);
        adapter.addFragment(mUserSearchFragment,  getResources().getText(R.string.users_search_tab_title).toString());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                {
                    //BattleName Search
                    mBattleNameSearchFragment.setQueryChangeListener(mSearchView);
                    mBattleNameSearchFragment.onQueryTextChange("");
                }
                else if (position == 1)
                {
                    //User Search
                    mUserSearchFragment.setQueryChangeListener(mSearchView);
                    mUserSearchFragment.onQueryTextChange("");

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
