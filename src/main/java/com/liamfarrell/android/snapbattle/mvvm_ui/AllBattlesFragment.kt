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
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.adapters.AllBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.adapters.FollowingListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.databinding.FragmentViewFollowersBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.AllBattlesViewModel
import com.liamfarrell.android.snapbattle.viewmodels.FollowingViewModel
import kotlinx.android.synthetic.main.fragment_friends_battle_list.*
import kotlinx.android.synthetic.main.fragment_friends_battle_list.parentCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_view_comments.*
import javax.inject.Inject

class AllBattlesFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AllBattlesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AllBattlesViewModel::class.java)
        val adapter = AllBattlesFeedPagingListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: AllBattlesFeedPagingListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { allBattlesResult ->
            adapter.submitList(allBattlesResult)
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {snackBarMessage ->
            Snackbar.make(parentCoordinatorLayout, snackBarMessage, Snackbar.LENGTH_LONG).show()
        })

        viewModel.networkErrors.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.noMoreOlderBattles.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                noMoreBattlesTextView.visibility = View.VISIBLE
            }
        })

        viewModel.isLoadingMoreBattles.observe(viewLifecycleOwner, Observer {
            val yo = it

        })
    }

}