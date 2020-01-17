package com.liamfarrell.android.snapbattle.mvvm_ui.host_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.liamfarrell.android.snapbattle.R
import timber.log.Timber

class NavigationHostFragment : Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.i("On Create")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Timber.i("On View Create")
        val v = inflater.inflate(R.layout.fragment_navigation_host, container, false)
        return v
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //The deep link intents need to be removed, or else the deep linked fragment will always be loaded when this fragment is created
        activity?.intent?.removeExtra("android-support-nav:controller:deepLinkIds")
        activity?.intent?.removeExtra("android-support-nav:controller:deepLinkIntent")
        activity?.intent?.removeExtra("android-support-nav:controller:deepLinkExtras")
    }
}