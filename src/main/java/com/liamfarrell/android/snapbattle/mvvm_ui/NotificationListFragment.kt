package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.adapters.NotificationPagedListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentNotificationListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.NotificationsViewModel
import javax.inject.Inject

class NotificationListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: NotificationsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NotificationsViewModel::class.java)
        val adapter = NotificationPagedListAdapter()
        binding.recyclerView.adapter = adapter

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: NotificationPagedListAdapter) {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notificationsList ->
            adapter.submitList(notificationsList)
            if (this.isVisible){
                viewModel.updateSeenAllBattles()
            }
        })
    }

}