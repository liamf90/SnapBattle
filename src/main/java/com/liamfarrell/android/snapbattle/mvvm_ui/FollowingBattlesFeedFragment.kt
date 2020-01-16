package com.liamfarrell.android.snapbattle.mvvm_ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.adapters.FollowingBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.viewmodels.FollowingBattlesFeedViewModel
import kotlinx.android.synthetic.main.fragment_friends_battle_list.*
import javax.inject.Inject

class FollowingBattlesFeedFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: FollowingBattlesFeedViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(FollowingBattlesFeedViewModel::class.java)
        val adapter = FollowingBattlesFeedPagingListAdapter(::onBattleLoadedByAdapter)
        adapter.setHasStableIds(true)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.recyclerView.itemAnimator = null
        binding.swipeContainer.setOnRefreshListener {onSwipeToRefresh()}
        subscribeUi(binding, adapter)
        registerReceiver()
        return binding.root
    }

    private fun subscribeUi(binding : FragmentFriendsBattleListBinding, adapter: FollowingBattlesFeedPagingListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { followingBattleResult ->
            adapter.submitList(followingBattleResult)
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer { snackBarMessage ->
            Snackbar.make(parentCoordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG).show()
        })

        viewModel.networkErrors.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.isNoBattlesInFeed.observe(viewLifecycleOwner, Observer {
            if (it) binding.NoBattlesTextView.visibility = View.VISIBLE
            else binding.NoBattlesTextView.visibility = View.GONE
        })

        viewModel.spinner.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it
        })

    }

    private fun onSwipeToRefresh(){
        viewModel.updateFollowingBattles()
    }

    private fun onBattleLoadedByAdapter(battle: Battle){
        viewModel.loadThumbnail(battle)
    }

    /**
     * When the home button is pressed, a broadcast is sent to scroll up the battle feeds
     */
    private fun registerReceiver() {
        //register receivers to update the list when a video is submitted (if fragment are still visible)
        val filter = IntentFilter()
        filter.addAction(MainActivity.HOME_BUTTON_PRESSED_INTENT)
        activity?.registerReceiver(onScrollUpBroadcastReceiver, filter)
    }

    private val onScrollUpBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isVisible){
            recycler_view.scrollToPosition(0)}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(onScrollUpBroadcastReceiver)
    }

}