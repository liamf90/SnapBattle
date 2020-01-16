package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.NotificationPagedListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentNotificationListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.notifications.Notification
import com.liamfarrell.android.snapbattle.viewmodels.NotificationsViewModel
import javax.inject.Inject


class NotificationListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: NotificationsViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentNotificationListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(NotificationsViewModel::class.java)
        val adapter = NotificationPagedListAdapter(::onNotificationLoadedByAdapter)
        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.viewModel = viewModel
        subscribeUi(binding, adapter)
        setToolbar(binding.includeToolbar.toolbar,  context?.getString(R.string.notifications_title) ?: "")
        return binding.root
    }

    private fun subscribeUi(binding : FragmentNotificationListBinding, adapter: NotificationPagedListAdapter) {
        viewModel.notifications.observe(viewLifecycleOwner, Observer { notificationsList ->
            adapter.submitList(notificationsList)
            if (this.isVisible){
                viewModel.updateSeenAllBattles()
            }
        })

        viewModel.isNoNotifications.observe(viewLifecycleOwner, Observer {
            if (it) binding.noNotificationsTextView.visibility = View.VISIBLE
            else binding.noNotificationsTextView.visibility = View.GONE
        })
    }

    private fun onNotificationLoadedByAdapter(notification: Notification){
        viewModel.getProfilePic(notification.opponentCognitoId)
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar, title: String){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = title
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }



}