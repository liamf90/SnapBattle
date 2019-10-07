package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.adapters.NotificationPagedListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentNotificationListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.NotificationsViewModel
import javax.inject.Inject
import androidx.recyclerview.widget.DividerItemDecoration
import com.liamfarrell.android.snapbattle.notifications.Notification


class NotificationListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: NotificationsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(NotificationsViewModel::class.java)
        val adapter = NotificationPagedListAdapter(::onNotificationLoadedByAdapter)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

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

    private fun onNotificationLoadedByAdapter(notification: Notification){
        viewModel.getProfilePic(notification.opponentCognitoId)
    }


}