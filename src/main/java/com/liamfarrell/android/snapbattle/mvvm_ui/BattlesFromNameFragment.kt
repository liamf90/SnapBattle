package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amazonaws.mobile.client.AWSMobileClient
import com.liamfarrell.android.snapbattle.adapters.AllBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.adapters.CompletedBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.BattlesByNameViewModelFactoryModule
import com.liamfarrell.android.snapbattle.di.CommentViewModelFactoryModule
import com.liamfarrell.android.snapbattle.di.DaggerBattlesByNameComponent
import com.liamfarrell.android.snapbattle.ui.ViewBattlesFromNameActivity
import com.liamfarrell.android.snapbattle.viewmodels.BattlesByNameViewModel
import com.liamfarrell.android.snapbattle.viewmodels.BattlesByNameViewModelFactory

class BattlesFromNameFragment : Fragment() {


    private lateinit var viewModel: BattlesByNameViewModel


    private var battleName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        battleName = getActivity()?.getIntent()?.getStringExtra(ViewBattlesFromNameActivity.EXTRA_BATTLE_NAME) ?: ""

        val appComponent = DaggerBattlesByNameComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .battlesByNameViewModelFactoryModule(BattlesByNameViewModelFactoryModule(battleName))
                .build()


        viewModel = ViewModelProviders.of(this, appComponent.getBattlesByNameViewModelFactory()).get(BattlesByNameViewModel::class.java)

        val adapter = AllBattlesFeedPagingListAdapter()
        binding.recyclerView.adapter = adapter
        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: AllBattlesFeedPagingListAdapter) {
        viewModel.battlesList.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }


}