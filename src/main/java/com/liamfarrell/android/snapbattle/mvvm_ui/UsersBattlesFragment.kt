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
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.MainActivity
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.UsersBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentUserBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.UsersBattlesViewModel
import javax.inject.Inject

class UsersBattlesFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UsersBattlesViewModel

    private var cognitoId: String? = null
    private var facebookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString("cognitoId")?.let { cognitoId = it} ?: arguments?.getString("facebookId")?.let {facebookId = it}
        ?: throw Error("CognitoId or FacebookId not specified")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(UsersBattlesViewModel::class.java)
        val adapter = UsersBattlesListAdapter()
        binding.includedList.recyclerList.adapter = adapter
        binding.userInfo.followUserClickListener = View.OnClickListener{viewModel.followUser()}
        binding.userInfo.unfollowUserClickListener = View.OnClickListener{viewModel.unfollowUser()}
        subscribeUi(binding, adapter)
        registerReceivers()
        if (viewModel.battles.value == null) {
            cognitoId?.let{viewModel.setCognitoId(it)} ?: facebookId?.let { viewModel.setFacebookId(it)}
        }

        return binding.root
    }

    private fun subscribeUi(binding : FragmentUserBinding, adapter: UsersBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)

            if (battlesList.isEmpty()) binding.includedList.noBattlesTextView.visibility = View.VISIBLE
            else binding.includedList.noBattlesTextView.visibility = View.GONE
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            binding.userInfo.user = it
            binding.toolbar.title = it.username
        })

        viewModel.spinner.observe(viewLifecycleOwner, Observer {
            if (it){
                binding.includedList.completedListProgressContainer.visibility = View.VISIBLE
            } else {
                binding.includedList.completedListProgressContainer.visibility = View.GONE
            }
        })
    }

    /**
     * When the home button is pressed, a broadcast is sent. If this fragment is visible, navigate back up
     */
    private fun registerReceivers() {
        //register receivers to update the list when a video is submitted (if fragment are still visible)
        val filterHomePressed = IntentFilter()
        filterHomePressed.addAction(MainActivity.HOME_BUTTON_PRESSED_INTENT)
        activity?.registerReceiver(onHomePressedBroadcastReceiver, filterHomePressed)
        val filterSearchPressed = IntentFilter()
        filterSearchPressed.addAction(MainActivity.SEARCH_BUTTON_PRESSED_INTENT)
        activity?.registerReceiver(onSearchPressedBroadcastReceiver, filterSearchPressed)
    }

    private val onHomePressedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == MainActivity.HOME_BUTTON_PRESSED_INTENT){
                if (isVisible && findNavController().graph.startDestination == R.id.navigation_home){
                    findNavController().navigate(R.id.navigation_home)}
            }
    }}

    private val onSearchPressedBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
                if (isVisible && findNavController().graph.startDestination == R.id.searchUsersAndBattlesFragment) {
                    findNavController().navigate(R.id.searchUsersAndBattlesFragment)
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(onHomePressedBroadcastReceiver)
    }



}