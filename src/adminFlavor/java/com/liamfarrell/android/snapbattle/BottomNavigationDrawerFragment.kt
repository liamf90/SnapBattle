package com.liamfarrell.android.snapbattle

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.liamfarrell.android.snapbattle.BuildConfig
import com.liamfarrell.android.snapbattle.R
import kotlinx.android.synthetic.main.fragment_bottomsheet.*

class BottomNavigationDrawerFragment: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navigation_view.setNavigationItemSelectedListener { menuItem ->
            // Bottom Navigation Drawer menu item clicks
            when (menuItem.itemId) {
                R.id.nav_create_battle -> findNavController().navigate(R.id.chooseBattleTypeFragment)
                R.id.nav_current_battles -> findNavController().navigate(R.id.battleCurrentListFragment)
                R.id.nav_completed_battles -> findNavController().navigate(R.id.battleCompletedListFragment)
                R.id.nav_challenges -> findNavController().navigate(R.id.battleChallengesListFragment2)
                R.id.nav_profile -> context!!.toast("dd")
                R.id.nav_add_followers -> findNavController().navigate(R.id.battleCurrentListFragment)
                R.id.nav_view_followers -> findNavController().navigate(R.id.viewFollowingFragment)
                R.id.nav_admin_battle_reportings -> context!!.toast("dd")
                R.id.nav_admin_comment_reportings -> context!!.toast("dd")
                R.id.nav_logout -> context!!.toast("dd")
            }
            // Add code here to update the UI based on the item selected
            // For example, swap UI fragments here
            true
        }
    }

    // This is an extension method for easy Toast call
    fun Context.toast(message: CharSequence) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.BOTTOM, 0, 600)
        toast.show()
    }

}