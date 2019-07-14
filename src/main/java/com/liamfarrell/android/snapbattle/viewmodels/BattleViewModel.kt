package com.liamfarrell.android.snapbattle.viewmodels

import android.app.PendingIntent.getActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.liamfarrell.android.snapbattle.R
import com.liamfarrell.android.snapbattle.app.App
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Voting
import com.liamfarrell.android.snapbattle.ui.FacebookLoginFragment
import java.util.*

class BattleViewModel(val battle: Battle) : ViewModel() {


    val canVote = MutableLiveData<Boolean>()


    init {
        if (battle.voting.votingState == Voting.VotingState.VOTING_STILL_GOING){
            if (battle.userHasVoted == true){
                canVote.value = false
            } else{
                battle.voting.canUserVote(FacebookLoginFragment.getCredentialsProvider(App.getContext()).cachedIdentityId, battle.challengerCognitoID, battle.challengedCognitoID, battle.challengerFacebookUserId, battle.challengedFacebookUserId, object : Voting.MutualFriendCallbacks {
                    override fun onCanVote() {
                        canVote.value = true
                    }

                    override fun onCannotVote() {
                        canVote.value = false
                    }
                })

            }
        }

    }
}