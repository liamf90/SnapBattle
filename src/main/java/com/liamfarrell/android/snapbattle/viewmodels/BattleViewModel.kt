package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Voting

class BattleViewModel(val battle: Battle) : ViewModel() {


    val canVote = MutableLiveData<Boolean>()


    init {
        if (battle.voting.votingState == Voting.VotingState.VOTING_STILL_GOING){
            if (battle.userHasVoted == true){
                canVote.value = false
            } else{
                battle.voting.canUserVote(IdentityManager.getDefaultIdentityManager().cachedUserID, battle.challengerCognitoID, battle.challengedCognitoID, battle.challengerFacebookUserId, battle.challengedFacebookUserId, object : Voting.MutualFriendCallbacks {
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