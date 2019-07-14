package com.liamfarrell.android.snapbattle.mvvm_ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.liamfarrell.android.snapbattle.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.liamfarrell.android.snapbattle.adapters.FollowFacebookFriendsListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentAddFollowersBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.FollowFacebookFriendsViewModel
import java.util.*


class FollowFacebookFriendsFragment : Fragment() {


    private lateinit var viewModel: FollowFacebookFriendsViewModel
    private val callbackManager: CallbackManager by lazy {CallbackManager.Factory.create()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentAddFollowersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner


        val appComponent = DaggerFacebookFollowingComponent.builder()
                .aWSLambdaModule(AWSLambdaModule(requireContext()))
                .followFacebookFriendViewModelFactoryModule(FollowFacebookFriendViewModelFactoryModule {requestUserFriendsPermission()})
                .build()

        viewModel = ViewModelProviders.of(this, appComponent.getFollowFacebookFriendsViewModelFactory()).get(FollowFacebookFriendsViewModel::class.java)
        val adapter = FollowFacebookFriendsListAdapter(viewModel)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.viewModel = viewModel

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: FollowFacebookFriendsListAdapter) {
        viewModel.following.observe(viewLifecycleOwner, Observer { followingList ->
            adapter.submitList(followingList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun requestUserFriendsPermission() {
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        viewModel.getFacebookFriends() }
                    override fun onCancel() {
                        //User Friends Not accepted.. Do nothing
                    }
                    override fun onError(e: FacebookException) {
                        Toast.makeText(activity, R.string.server_error_toast, Toast.LENGTH_SHORT).show()
                    }
                })
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("user_friends"))
    }


}