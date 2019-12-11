package com.liamfarrell.android.snapbattle.model;

import android.content.Context;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class Voting implements Serializable {
    public enum ChallengerOrChallenged
    {
        CHALLENGER,
        CHALLENGED
    }
    public enum VotingState{
        NO_VOTING,
        VOTING_NOT_YET_BEGUN,
        VOTING_STILL_GOING,
        VOTING_FINISHED
    }

    public interface MutualFriendCallbacks
    {
        void onCanVote();
        void onCannotVote();
    }


    private ChooseVotingFragment.VotingChoice mVotingChoice;
    private ChooseVotingFragment.VotingLength mVotingLength;
    private Date mVotingTimeEnd;
    private Integer mVoteChallenger;
    private Integer mVoteChallenged;

    public Voting(ChooseVotingFragment.VotingChoice votingChoice, ChooseVotingFragment.VotingLength votingLength, Date votingTimeEnd, Integer voteChallenger, Integer voteChallenged) {
        mVotingChoice = votingChoice;
        mVotingLength = votingLength;
        mVotingTimeEnd = votingTimeEnd;
        mVoteChallenger = voteChallenger;
        mVoteChallenged = voteChallenged;
    }

    public ChooseVotingFragment.VotingChoice getVotingChoice() {
        return mVotingChoice;
    }

    public ChooseVotingFragment.VotingLength getVotingLength() {
        return mVotingLength;
    }

    public Date getVotingTimeEnd() {
        return mVotingTimeEnd;
    }

    public Integer getVoteChallenger() {
        return mVoteChallenger;
    }

    public Integer getVoteChallenged() {
        return mVoteChallenged;
    }

    public String getChallengerVotingResult(Context context)
    {
        if (getVoteChallenger() > getVoteChallenged())
        {
            return context.getResources().getString(R.string.winner);
        }
        else if (getVoteChallenger() < getVoteChallenged())
        {
            return context.getResources().getString(R.string.loser);
        }
        else
        {
            return context.getResources().getString(R.string.draw);
        }
    }
    public String getChallengedVotingResult(Context context)
    {
        if (getVoteChallenged() > getVoteChallenger())
        {
            return context.getResources().getString(R.string.winner);		}
        else if (getVoteChallenged() < getVoteChallenger())
        {
            return context.getResources().getString(R.string.loser);
        }
        else
        {
            return context.getResources().getString(R.string.draw);
        }
    }

    public void setVotingTimeEnd(String votingTimeEnd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            mVotingTimeEnd = sdf.parse(votingTimeEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public VotingState getVotingState(){
        if (mVotingChoice == ChooseVotingFragment.VotingChoice.NONE){
            return VotingState.NO_VOTING;
        }

        if (mVotingTimeEnd == null){
            return VotingState.VOTING_NOT_YET_BEGUN;
        }

        if (mVotingTimeEnd.after(new Date(System.currentTimeMillis()))){
            return VotingState.VOTING_STILL_GOING;
        } else {
            return VotingState.VOTING_FINISHED;
        }
    }
    public void canUserVote(String currentUserCognitoId, String challengerCognitoId, String challengedCognitoId, String challengerFacebookId, String challengedFacebookId, Voting.MutualFriendCallbacks callback)
    {

        if (currentUserCognitoId.equals(challengerCognitoId) || currentUserCognitoId.equals(challengedCognitoId))
        {
            callback.onCannotVote();
            return;
        }


        if (getVotingChoice() == ChooseVotingFragment.VotingChoice.PUBLIC)
        {
            if (isTimeLeftToVote()){
                callback.onCanVote();
            } else {
                callback.onCannotVote();
            }
        }
		/* FUTURE RELEASE
		else if (super.getVoting().getVotingChoice() == ChooseVotingFragment.VotingChoice.SELECTED)
		{
			//Get list of selected opponents. From JSON. IF current user facbeook id is in list of selected facebook ids. return true
			if (isTimeLeftToVote())
			{

				String facebookIDCurrent = AccessToken.getCurrentAccessToken().getUserId();


				for (int x=0; x < mSelectedJudgesFacebookIDList.size(); x++)
				{
					 if (mSelectedJudgesFacebookIDList.get(x).equals(facebookIDCurrent))
					 {
						 return true;
					 }
				}

				return false;
			}
			else
			{
				return false;
			}
		}
		*/
        else if (getVotingChoice() == ChooseVotingFragment.VotingChoice.NONE)
        {
            callback.onCannotVote();
        }
        else if (getVotingChoice() == ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK)
        {
            //IF TIME LEFT. AND
            //IF CHALLENGER AND CHALLENGED FACEBOOK ID IS ON THE USERS FRIENDS LIST. RETURN TRUE
            //ELSE RETURN FALSE
            if (isTimeLeftToVote())
            {
                checkIfFacebookFriendWithChallengerAndChallenged(challengerFacebookId, challengedFacebookId, callback);
            }
            else
            {
                callback.onCannotVote();
            }
        }
        else
        {
            callback.onCannotVote();
        }
    }



    private boolean isTimeLeftToVote()
    {
        //Current Time
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date timeNow = cal.getTime();

        //End Time
        if (getVotingTimeEnd().getTime() - timeNow.getTime() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void checkIfFacebookFriendWithChallengerAndChallenged(String challengerFacebookID, String challengedFacebookID, final Voting.MutualFriendCallbacks callback)
    {
        //this method returns on the callback if the user is facebook friends with the challenger and the challenged user.
        //this method is used to show weather the user can vote or not in an battle with mutual facebook friends only voting option selected


        final String graphPath = "/me/friends/" + challengerFacebookID;
        final String graphPath2 = "/me/friends/" + challengedFacebookID;

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                graphPath,
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response)
                    {
                        JSONArray friendsList;
                        try {
                            friendsList = response.getJSONObject().getJSONArray("data");
                            //if the friends list length > 0 than the users are facebook friends
                            if (friendsList.length()  != 0)
                            {
                                new GraphRequest(
                                        AccessToken.getCurrentAccessToken(),
                                        graphPath2,
                                        null,
                                        HttpMethod.GET,
                                        new GraphRequest.Callback() {
                                            public void onCompleted(GraphResponse response)
                                            {
                                                JSONArray friendsList;
                                                try {
                                                    friendsList = response.getJSONObject().getJSONArray("data");
                                                    //if the friends list length > 0 than the users are facebook friends
                                                    if (friendsList.length()  != 0)
                                                    {
                                                        //if the code has got this far, the user is facebook friends with
                                                        //the challenger and challenged user of the battle.
                                                        callback.onCanVote();
                                                    }
                                                    else
                                                    {
                                                        callback.onCannotVote();
                                                    }

                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }).executeAsync();
                            }else {
                                callback.onCannotVote();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
        ).executeAsync();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Voting voting = (Voting) o;
        return mVotingChoice == voting.mVotingChoice &&
                mVotingLength == voting.mVotingLength &&
                Objects.equals(mVotingTimeEnd, voting.mVotingTimeEnd) &&
                Objects.equals(mVoteChallenger, voting.mVoteChallenger) &&
                Objects.equals(mVoteChallenged, voting.mVoteChallenged);
    }

}
