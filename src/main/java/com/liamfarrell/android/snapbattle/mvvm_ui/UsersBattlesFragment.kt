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
import com.liamfarrell.android.snapbattle.adapters.UsersBattlesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentUserBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.UsersBattlesViewModel
import kotlinx.android.synthetic.main.fragment_completed_list.view.*
import javax.inject.Inject

class UsersBattlesFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: UsersBattlesViewModel
    private lateinit var userCognitoID : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentUserBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(UsersBattlesViewModel::class.java)
        val adapter = UsersBattlesListAdapter()
        binding.includedList.recyclerList.adapter = adapter
        binding.userInfo.followUserClickListener = View.OnClickListener{viewModel.followUser()}
        binding.userInfo.unfollowUserClickListener = View.OnClickListener{viewModel.unfollowUser()}
        subscribeUi(binding, adapter)
        viewModel.setCognitoId(userCognitoID)
        return binding.root
    }

    private fun subscribeUi(binding : FragmentUserBinding, adapter: UsersBattlesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            binding.userInfo.user = it
            binding.toolbar.title = it.username
        })
    }

}