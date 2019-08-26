package com.liamfarrell.android.snapbattle.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.mvvm_ui.AllBattlesFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.FollowingBattlesFeedFragment

private val TAB_TITLES = arrayOf(
        R.string.all_battles_tab_title,
        R.string.following_battles_tab_title
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class HomePagerAdapter(private val context: Context, fm: FragmentManager)
    : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return if (position == 0){
            AllBattlesFragment()
        } else{
            FollowingBattlesFeedFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show 2 total pages.
        return 2
    }
}