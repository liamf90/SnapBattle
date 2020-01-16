package com.liamfarrell.android.snapbattle.mvvm_ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.FollowFacebookFriendsListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentAddFollowersBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.viewmodels.AddFacebookFriendsAsFollowersViewModel
import java.util.*
import javax.inject.Inject


class FollowFacebookFriendsFragment : Fragment() , Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: AddFacebookFriendsAsFollowersViewModel
    private val callbackManager: CallbackManager by lazy {CallbackManager.Factory.create()}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentAddFollowersBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this, viewModelFactory).get(AddFacebookFriendsAsFollowersViewModel::class.java)
        val adapter = FollowFacebookFriendsListAdapter(::addFollower, ::removeFollower)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.viewModel = viewModel
        setToolbar(binding.includeToolbar.toolbar,  context?.getString(R.string.nav_add_followers) ?: "")
        subscribeUi(binding, adapter)
        viewModel.getFacebookFriendsWithFollowing (::requestUserFriendsPermission)
        return binding.root
    }

    private fun subscribeUi( binding : FragmentAddFollowersBinding, adapter: FollowFacebookFriendsListAdapter) {
        viewModel.following.observe(viewLifecycleOwner, Observer { followingList ->
                followingList?.let{
                    adapter.submitList(followingList.toList())
                    adapter.notifyDataSetChanged()

                    if (followingList.isEmpty()) binding.noFacebookFriendsTextView.visibility = View.VISIBLE
                    else binding.noFacebookFriendsTextView.visibility = View.GONE
                }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })
    }

    private fun addFollower(facebookUserId: String){
        viewModel.addFollowing(facebookUserId)
    }

    private fun removeFollower(cognitoId:String){
        viewModel.removeFollowing(cognitoId)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun setToolbar(toolbar : androidx.appcompat.widget.Toolbar, title: String){
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = title
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun requestUserFriendsPermission() {
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        viewModel.getFacebookFriends(::requestUserFriendsPermission) }
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