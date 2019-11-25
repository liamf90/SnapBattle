package com.liamfarrell.android.snapbattle.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amazonaws.mobile.auth.core.IdentityManager
import com.liamfarrell.android.snapbattle.model.Battle
import com.liamfarrell.android.snapbattle.model.Voting

class BattleViewModel(val battle: Battle) : ViewModel() {


    enum class UserCanVote {
        HAS_VOTED,
        CANNOT_VOTE,
        CAN_VOTE
    }

    val canVoteLiveData = MutableLiveData<UserCanVote>()


    init {
        if (battle.voting.votingState == Voting.VotingState.VOTING_STILL_GOING){
            if (battle.userHasVoted == true){
                canVoteLiveData.value = UserCanVote.HAS_VOTED
            } else{
                battle.voting.canUserVote(IdentityManager.getDefaultIdentityManager().cachedUserID, battle.challengerCognitoID, battle.challengedCognitoID, battle.challengerFacebookUserId, battle.challengedFacebookUserId, object : Voting.MutualFriendCallbacks {
                    override fun onCanVote() {
                        canVoteLiveData.value = UserCanVote.CAN_VOTE
                    }

                    override fun onCannotVote() {
                        canVoteLiveData.value = UserCanVote.CANNOT_VOTE
                    }
                })

            }
        }

    }
}