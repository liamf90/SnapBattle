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
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.adapters.CurrentBattlesListAdapter
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.viewmodels.CurrentBattlesViewModel
import javax.inject.Inject

class BattleCurrentListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private lateinit var viewModel: CurrentBattlesViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentBattlesViewModel::class.java)
        val adapter = CurrentBattlesListAdapter(IdentityManager.getDefaultIdentityManager().cachedUserID)
        binding.recyclerView.adapter = adapter
        binding.showSpinner = viewModel.spinner

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: CurrentBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            battlesList?.let{adapter.submitList(battlesList)}
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }


}