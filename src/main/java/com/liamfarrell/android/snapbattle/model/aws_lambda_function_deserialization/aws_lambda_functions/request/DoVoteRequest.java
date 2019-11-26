package com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request;

import com.liamfarrell.android.snapbattle.model.Voting;

public class DoVoteRequest
{
    private int battleID;
    private Voting.ChallengerOrChallenged challengerOrChallenged;
    private String facebookToken;

    public int getBattleID() {
        return battleID;
    }

    public void setBattleID(int battleID) {
        this.battleID = battleID;
    }

    public Voting.ChallengerOrChallenged getChallengerOrChallenged() {
        return challengerOrChallenged;
    }

    public void setChallengerOrChallenged(Voting.ChallengerOrChallenged challengerOrChallenged) {
        this.challengerOrChallenged = challengerOrChallenged;
    }

    public String getFacebookToken() {
        return facebookToken;
    }

    public void setFacebookToken(String facebookToken) {
        this.facebookToken = facebookToken;
    }
}
