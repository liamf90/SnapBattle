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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.adapters.CurrentBattlesListAdapter
import com.liamfarrell.android.snapbattle.data.OtherUsersProfilePicUrlRepository
import com.liamfarrell.android.snapbattle.databinding.FragmentCurrentBattleListBinding
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.testing.OpenForTesting
import com.liamfarrell.android.snapbattle.viewmodels.CurrentBattlesViewModel
import kotlinx.android.synthetic.main.fragment_friends_battle_list.*
import javax.inject.Inject

@OpenForTesting
class BattleCurrentListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private lateinit var viewModel: CurrentBattlesViewModel

    lateinit var binding : FragmentCurrentBattleListBinding

    @Inject
    lateinit var identityManager: IdentityManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentCurrentBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(CurrentBattlesViewModel::class.java)
        val adapter = CurrentBattlesListAdapter(getCognitoId())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.showSpinner = viewModel.spinner

        subscribeUi(binding, adapter)
        return binding.root
    }

    private fun subscribeUi(binding : FragmentCurrentBattleListBinding, adapter: CurrentBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            battlesList?.let{
                adapter.submitList(battlesList)

                if (it.isEmpty()) binding.NoBattlesTextView.visibility = View.VISIBLE
                else binding.NoBattlesTextView.visibility = View.GONE
            }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }


    fun getCognitoId() : String {
        return identityManager.cachedUserID
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()


}