package com.liamfarrell.android.snapbattle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.HomeHostFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.NavigationHostFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.NotificationHostFragment
import com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments.SearchHostFragment
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject



interface HideAndShowBottomNavigation{
    fun hideBottomNavigation()
    fun showBottomNavigation()
}


class MainActivity : AppCompatActivity(), HasSupportFragmentInjector, HideAndShowBottomNavigation {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private val notificationHostFragment : NotificationHostFragment by lazy {NotificationHostFragment()}
    private val navigationHostFragmnet : NavigationHostFragment by lazy { NavigationHostFragment() }
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
                loadFragment(NotificationHostFragment())
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

        //if there is a deep link intent show the deep link destination in the side menu, else show the home fragment
        if (intent.hasExtra("android-support-nav:controller:deepLinkIntent")){
            navView.selectedItemId = R.id.side_menu
        } else {
            navView.selectedItemId = R.id.navigation_home
        }
    }



    private fun loadFragment(fragment: Fragment) {
        // load fragment
        val transaction = supportFragmentManager.beginTransaction()
        supportFragmentManager.primaryNavigationFragment?.let{transaction.hide(it)}
        if (supportFragmentManager.fragments.contains(fragment)){
            transaction.show(fragment)
        } else {
            transaction.add(R.id.nav_host_container, fragment) }
        transaction.setPrimaryNavigationFragment(fragment)
        transaction.commit()
    }


    override fun hideBottomNavigation() {
        bottom_nav.visibility = View.GONE
    }

    override fun showBottomNavigation() {
        bottom_nav.visibility = View.VISIBLE
    }


    override fun supportFragmentInjector() = dispatchingAndroidInjector
}

