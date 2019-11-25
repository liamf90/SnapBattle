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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.BattlesByNamePagedListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentFriendsBattleListBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.BattlesByNameViewModel
import timber.log.Timber
import javax.inject.Inject

class BattlesFromNameFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: BattlesByNameViewModel


    private var battleName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        battleName = arguments?.getString("battleName") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentFriendsBattleListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BattlesByNameViewModel::class.java)
        if (viewModel.battleName.value == null){viewModel.setBattleName(battleName)}
        val adapter = BattlesByNamePagedListAdapter()
        binding.recyclerView.adapter = adapter
        binding.showSpinner = viewModel.spinnerLiveData
        binding.swipeContainer.isEnabled = false
        subscribeUi(adapter)
        registerReceiver()
        return binding.root
    }

    private fun subscribeUi(adapter: BattlesByNamePagedListAdapter) {
        viewModel.battlesList.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    /**
     * When the search button is pressed on the bottom navigation, a broadcast is sent. If this fragment is visible, navigate back up
     */
    private fun registerReceiver() {
        //register receivers to update the list when a video is submitted (if fragment are still visible)
        val filter = IntentFilter()
        filter.addAction(MainActivity.SEARCH_BUTTON_PRESSED_INTENT)
        activity?.registerReceiver(onSearchPressedBroadcastReceiver, filter)
    }

    private val onSearchPressedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
                if (isVisible && findNavController().graph.startDestination == R.id.searchUsersAndBattlesFragment) {
                    findNavController().navigate(R.id.searchUsersAndBattlesFragment)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(onSearchPressedBroadcastReceiver)
    }



}