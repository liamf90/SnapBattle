package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.*
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.fragment.app.Fragment
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.HomePagerAdapter
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_home_fragment, container, false)
        val sectionsPagerAdapter = HomePagerAdapter(requireContext(), childFragmentManager)
        val viewPager: ViewPager = view.findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = view.findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        return view
    }


}