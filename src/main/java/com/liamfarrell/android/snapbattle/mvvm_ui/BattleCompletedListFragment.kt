package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.adapters.CompletedBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.DaggerCompletedBattlesComponent
import com.liamfarrell.android.snapbattle.viewmodels.CompletedBattlesViewModel

class BattleCompletedListFragment : Fragment() {

    private lateinit var viewModel: CompletedBattlesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerCompletedBattlesComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .build()



        viewModel = ViewModelProviders.of(this, appComponent.getCompletedBattlesViewModelFactory()).get(CompletedBattlesViewModel::class.java)
        val adapter = CompletedBattlesListAdapter()
        binding.recyclerView.adapter = adapter

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: CompletedBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }

}