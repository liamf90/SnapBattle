package com.liamfarrell.android.snapbattle.mvvm_ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.adapters.BattleChallengesAdapterCallbacks
import com.liamfarrell.android.snapbattle.adapters.BattleChallengesListAdapter
import com.liamfarrell.android.snapbattle.databinding.FragmentChallengesListBinding
import com.liamfarrell.android.snapbattle.di.Injectable
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.ui.createbattle.ChooseVotingFragment
import com.liamfarrell.android.snapbattle.viewmodels.BattleChallengesViewModel
import java.util.*
import javax.inject.Inject

class BattleChallengesListFragment : Fragment(), BattleChallengesAdapterCallbacks, Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: BattleChallengesViewModel
    private lateinit var callbackManager: CallbackManager
    private val adapter = BattleChallengesListAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val binding = FragmentChallengesListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(BattleChallengesViewModel::class.java)
        binding.recyclerList.adapter = adapter

        subscribeUi(adapter)
        return binding.root
    }

    private fun subscribeUi(adapter: BattleChallengesListAdapter) {
        viewModel.battles.observe(viewLifecycleOwner, Observer { battlesList ->
            adapter.submitList(battlesList)
        })

        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onBattleAccepted(holder : BattleChallengesListAdapter.ViewHolder, battle: Battle) {
        if (battle.voting.votingChoice === ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK) {
            if (doesUserHaveUserFriendsPermission()) {
                viewModel.onBattleAccepted(battle)
            } else {
                Toast.makeText(activity, R.string.need_accept_permission_user_friends, Toast.LENGTH_SHORT).show()
                requestUserFriendsPermission(holder, battle)
            }
        } else {
            viewModel.onBattleAccepted(battle)
        }
        viewModel.onBattleAccepted(battle)
    }

    override fun onBattleDeclined(battle: Battle) {
        viewModel.onBattleDeclined(battle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun doesUserHaveUserFriendsPermission(): Boolean {
        val declinedPermissions = AccessToken.getCurrentAccessToken().declinedPermissions
        return !declinedPermissions.contains("user_friends")
    }

    private fun requestUserFriendsPermission(holder : BattleChallengesListAdapter.ViewHolder, battle: Battle) {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        viewModel.onBattleAccepted(battle)
                    }
                    override fun onCancel() {
                        //User Friends Permission Not accepted.. Do nothing
                        holder.setDeclineButtonEnabled(true)
                        holder.setAcceptButtonEnabled(true)
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