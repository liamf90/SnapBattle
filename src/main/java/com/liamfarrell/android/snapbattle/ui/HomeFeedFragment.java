package com.liamfarrell.android.snapbattle.ui;


/**
 * Created by Liam on 30/07/2017.
 */


import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.mvvm_ui.AllBattlesFragment;
import com.liamfarrell.android.snapbattle.mvvm_ui.FollowingBattlesFeedFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeFeedFragment extends Fragment {

    private AllBattlesFragment mAllBattlesListFragment;
    private FollowingBattlesFeedFragment mFollowingBattlesListFragment;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceBundle) {
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);

        Log.i("HomeFeed", "On Create View");
        if (mAllBattlesListFragment == null) {
            mAllBattlesListFragment = new AllBattlesFragment();
        }
        if (mFollowingBattlesListFragment == null) {
            mFollowingBattlesListFragment = new FollowingBattlesFeedFragment();
        }

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("HomeFeed", "On Destroy");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.i("HomeFeed", "On Create");

    }






    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(mAllBattlesListFragment, getResources().getText(R.string.all_battles_tab_title).toString());
        adapter.addFragment(mFollowingBattlesListFragment,  getResources().getText(R.string.following_battles_tab_title).toString());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                {
                    Log.i("HomeFeed", "TAB 0 selected");
                    //Full Feed
                   // mAllBattlesListFragment.updateAllBattlesList();
                }
                else if (position == 1)
                {
                    //Following Feed
                    Log.i("HomeFeed", "TAB 1 selected");
                    //mFollowingBattlesListFragment.updateFollowingList();


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

        @Override
        public Parcelable saveState() {
            return super.saveState();
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
