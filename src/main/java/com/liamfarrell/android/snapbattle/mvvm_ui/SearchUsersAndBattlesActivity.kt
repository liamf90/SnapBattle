package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.SectionsPagerAdapter
import com.liamfarrell.android.snapbattle.ui.UserSearchFragment

class SearchUsersAndBattlesActivity : AppCompatActivity(){

    private var battleNameSearchFragment = com.liamfarrell.android.snapbattle.mvvm_ui.BattleNameSearchFragment()
    private var userSearchFragment = com.liamfarrell.android.snapbattle.mvvm_ui.UserSearchFragment()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_users_and_battles2)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val searchItem =  menu.findItem(R.id.action_search)
        searchItem.expandActionView()
        val searchView = searchItem.actionView as SearchView
        setupViewPager(searchView)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupViewPager(searchView : SearchView){
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager, searchView)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
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
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            this.finish()
        }
        return true
    }
}