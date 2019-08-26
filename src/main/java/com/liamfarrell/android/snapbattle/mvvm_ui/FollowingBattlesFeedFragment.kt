package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.liamfarrell.android.snapbattle.adapters.AllBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.adapters.FollowingBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.AllBattlesViewModel
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

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(FollowingBattlesFeedViewModel::class.java)
        val adapter = FollowingBattlesFeedPagingListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: FollowingBattlesFeedPagingListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { followingBattleResult ->
            adapter.submitList(followingBattleResult)
        })

        viewModel.networkErrors.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

    }

}