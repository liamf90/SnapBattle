package com.liamfarrell.android.snapbattle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.liamfarrell.android.snapbattle.mvvm_ui.*
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.HomeHostFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.NavigationHostFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.SearchHostFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private val notificationListFragment : NavigationHostFragment by lazy { NavigationHostFragment() }
    private val searchUsersAndBattlesFragment : SearchHostFragment by lazy { SearchHostFragment() }
    private val homeFragment : HomeHostFragment by lazy { HomeHostFragment() }


    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.side_menu -> {
                loadFragment(NavigationHostFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_home -> {
                loadFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                loadFragment(searchUsersAndBattlesFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                loadFragment(notificationListFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
       navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
       navView.selectedItemId = R.id.navigation_home
    }



    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    override fun supportFragmentInjector() = dispatchingAndroidInjector
}

