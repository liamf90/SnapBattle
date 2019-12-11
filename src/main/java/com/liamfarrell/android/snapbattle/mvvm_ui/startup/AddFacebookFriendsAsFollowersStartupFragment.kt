package com.liamfarrell.android.snapbattle.mvvm_ui.startup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.liamfarrell.android.snapbattle.R
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.material.snackbar.Snackbar
import com.liamfarrell.android.snapbattle.adapters.AddFacebookFriendsAsFollowersStartupListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentAddFollowersSelectBinding
import com.liamfarrell.android.snapbattle.di.*
import com.liamfarrell.android.snapbattle.viewmodels.AddFacebookFriendsAsFollowersViewModel
import java.lang.ClassCastException
import java.util.*
import javax.inject.Inject


class AddFacebookFriendsAsFollowersStartupFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var startupActivity : SetupToolbarInterface
    private lateinit var viewModel: AddFacebookFriendsAsFollowersViewModel
    private val callbackManager: CallbackManager by lazy {CallbackManager.Factory.create()}
    private val adapter = AddFacebookFriendsAsFollowersStartupListAdapter{enableNextButton -> updateShouldShowNextButton(enableNextButton)}


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SetupToolbarInterface){
            startupActivity = context
            startupActivity.setTitle(resources.getStringArray(R.array.startup_activity_titles)[0])
        } else {
            throw ClassCastException() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val binding = FragmentAddFollowersSelectBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        viewModel = ViewModelProvider(this, viewModelFactory).get(AddFacebookFriendsAsFollowersViewModel::class.java)
        binding.recyclerList.adapter = adapter
        binding.recyclerList.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        binding.viewModel = viewModel
        subscribeUi(binding, adapter)
        viewModel.getFacebookFriends(::requestUserFriendsPermission)
        return binding.root
    }



    private fun subscribeUi(binding: FragmentAddFollowersSelectBinding, adapter: AddFacebookFriendsAsFollowersStartupListAdapter) {
        viewModel.following.observe(viewLifecycleOwner, Observer { followingList ->
            followingList?.let {
                adapter.submitList(followingList) }
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let{Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
        })

        viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {
            viewModel.snackBarMessage.observe(viewLifecycleOwner, Observer {
                it?.let {
                    val coordinatorLayout = binding.coordinatorLayout
                    val snackBar = Snackbar.make(coordinatorLayout, it, Snackbar.LENGTH_SHORT)
                    snackBar.show()
                }
            })
        })
    }


    override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        val id = item.itemId
        if (id == R.id.action_next) {
            viewModel.addFollowing(::nextFragment)
        } else if (id == R.id.action_skip) {
            nextFragment()
        }
        return false
    }

    private fun nextFragment() {
        findNavController().navigate(R.id.action_addFacebookFriendsAsFollowersStartupFragment_to_chooseNameStartupFragment, arguments)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateShouldShowNextButton(show: Boolean){
        if (show){
            startupActivity.enableNextButton()
        } else {
            startupActivity.disableNextButton()
        }
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