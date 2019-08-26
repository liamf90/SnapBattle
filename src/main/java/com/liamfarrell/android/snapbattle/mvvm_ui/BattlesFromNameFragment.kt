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
import com.liamfarrell.android.snapbattle.adapters.AllBattlesFeedPagingListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.ui.ViewBattlesFromNameActivity
import com.liamfarrell.android.snapbattle.viewmodels.BattlesByNameViewModel
import javax.inject.Inject

class BattlesFromNameFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: BattlesByNameViewModel


    private var battleName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        battleName = getActivity()?.getIntent()?.getStringExtra(ViewBattlesFromNameActivity.EXTRA_BATTLE_NAME) ?: ""


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BattlesByNameViewModel::class.java)

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