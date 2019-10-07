package com.liamfarrell.android.snapbattle.adapters

import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


private val titles = mutableListOf<String>()
private val fragments = mutableListOf<Fragment>()

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SearchPagerAdapter(fm: FragmentManager, private val searchView: SearchView)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        fragments[position]
        return fragments[position]

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        titles.add(title)
    }

    fun deleteFragments(){
        fragments.clear()
    }
    override fun getCount(): Int {
        return fragments.size
    }
}
