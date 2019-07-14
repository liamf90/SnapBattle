package com.liamfarrell.android.snapbattle.mvvm_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.adapters.CompletedBattlesListAdapter
import com.liamfarrell.android.snapbattle.adapters.CurrentBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.AWSLambdaModule
import com.liamfarrell.android.snapbattle.di.DaggerCompletedBattlesComponent
import com.liamfarrell.android.snapbattle.di.DaggerCurrentBattlesComponent
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import com.liamfarrell.android.snapbattle.viewmodels.CompletedBattlesViewModel
import com.liamfarrell.android.snapbattle.viewmodels.CurrentBattlesViewModel
import kotlinx.android.synthetic.main.fragment_view_comments.*

class BattleCurrentListFragment : Fragment() {

    private lateinit var viewModel: CurrentBattlesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        val appComponent = DaggerCurrentBattlesComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .build()



        viewModel = ViewModelProviders.of(this, appComponent.getCurrentBattlesViewModelFactory()).get(CurrentBattlesViewModel::class.java)
        val adapter = CurrentBattlesListAdapter(FacebookLoginFragment.getCredentialsProvider(context).cachedIdentityId)
        binding.recyclerView.adapter = adapter

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: CurrentBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })
    }

}