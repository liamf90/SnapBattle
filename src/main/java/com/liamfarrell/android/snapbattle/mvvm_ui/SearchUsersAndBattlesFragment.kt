package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.*
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil.setContentView
import androidx.fragment.app.Fragment
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.SearchPagerAdapter

class SearchUsersAndBattlesFragment : Fragment(){

    private var battleNameSearchFragment = BattleNameSearchFragment()
    private var userSearchFragment = UserSearchFragment()

    private lateinit var viewPager: ViewPager
    private lateinit var tabs : TabLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_search_users_and_battles2, container, false)
        viewPager = view.findViewById(R.id.view_pager)
        tabs = view.findViewById(R.id.tabs)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)
        val searchItem =  menu.findItem(R.id.action_search)
        searchItem.expandActionView()
        val searchView = searchItem.actionView as SearchView
        setupViewPager(searchView)
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setupViewPager(searchView : SearchView){
        val sectionsPagerAdapter = SearchPagerAdapter(childFragmentManager, searchView)
        battleNameSearchFragment.setOnQueryChangedListener(searchView)
        sectionsPagerAdapter.addFragment(battleNameSearchFragment, resources.getString(R.string.battles_search_tab_title))
        sectionsPagerAdapter.addFragment(userSearchFragment, resources.getString(R.string.users_search_tab_title) )
        viewPager.adapter = sectionsPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    battleNameSearchFragment.setOnQueryChangedListener(searchView)
                    //battleNameSearchFragment.onQueryTextChange("")
                } else if (position == 1) {
                    userSearchFragment.setOnQueryChangedListener(searchView)
                    //userSearchFragment.onQueryTextChange("")
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })

        tabs.setupWithViewPager(viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            //childFragmentManager.bac
        }
        return true
    }
}