package com.liamfarrell.android.snapbattle.model;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.app.SnapBattleApp;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.UrlLambdaRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import timber.log.Timber;


@Entity(tableName = "all_battles")
public class Battle implements Serializable
{
	public static final String ORIENTATION_LOCK_PORTRAIT = "PORTRAIT";
	public static final String ORIENTATION_LOCK_LANDSCAPE = "LANDSCAPE";
	public static final String ORIENTATION_LOCK_UNDEFINED = "UNDEFINED";

	private static final String TAG = "Battle";


    @PrimaryKey
    @ColumnInfo(name = "battle_id")
    private Integer mBattleID;
    private String mBattleName;
    private String mChallengerCognitoId;
    private String mChallengedCognitoId;
    private Integer mRounds;

    private String mChallengerFacebookUserId;
    private String mChallengedFacebookUserId;
    private Boolean mBattleAccepted;

    private String mChallengerUsername;
    private String mChallengedUsername;
    private String mOpponentUsername;
    private String mChallengerName;
    private String mChallengedName;

    private Date mLastVideoUploadTime;
    private Integer mVideosUploaded;
    @Ignore
    private ArrayList<Video> mVideos;
    private Integer mVideoViewCount;
    @Embedded
    private Voting mVoting;
    private int mLikeCount;
    private int mDislikeCount;
    private Who_turn mWhoTurn;
    private Date mChallengedTime;
    private String mProfilePicSmallSignedUrl;
    private Integer mChallengerProfilePicCount;



    private Integer mChallengedProfilePicCount;
    private String mChallengerProfilePicSignedUrl;

    private String mOrientationLock;
    private boolean mDeleted;
    private boolean mIsFinalVideoReady;
    private int mCommentCount;
    private String mSignedThumbnailUrl;
    private Boolean mUserHasVoted;
    private Boolean mIsFollowingFeedBattle;

    public Battle(int battleID, String challengerCognitoId, String challengedCognitoId, String battleName, int rounds)
    {
        mBattleID = battleID;
        mChallengerCognitoId = challengerCognitoId;
        mChallengedCognitoId = challengedCognitoId;
        mBattleName = battleName;
        mRounds = rounds;
    }



    public String getCompletedBattleStatus(Context context)
    {
        if (mLastVideoUploadTime == null)
        {
            return "";
        }
        return Video.getTimeSince(context, mLastVideoUploadTime);
    }

    public Boolean getIsFollowingFeedBattle() {
        return mIsFollowingFeedBattle;
    }

    public void setIsFollowingFeedBattle(Boolean followingFeedBattle) {
        mIsFollowingFeedBattle = followingFeedBattle;
    }

    public Boolean getUserHasVoted() {
        return mUserHasVoted;
    }

    public void setUserHasVoted(boolean userHasVoted) {
        mUserHasVoted = userHasVoted;
    }

    public boolean getIsBattleAccepted() {
        return mBattleAccepted;
    }

    public boolean isDeleted() {
        return mDeleted;
    }

    public void setDeleted(boolean deleted) {
        mDeleted = deleted;
    }

    public boolean getIsFinalVideoReady() {
        return mIsFinalVideoReady;
    }

    public void setIsFinalVideoReady(boolean finalVideoReady) {
        mIsFinalVideoReady = finalVideoReady;
    }

    public Integer getVideoViewCount() {
        return mVideoViewCount;
    }

    public void setVideoViewCount(Integer videoViewCount) {
        mVideoViewCount = videoViewCount;
    }
    public Integer getChallengedProfilePicCount() {
        return mChallengedProfilePicCount;
    }

    public void setChallengedProfilePicCount(Integer challengedProfilePicCount) {
        mChallengedProfilePicCount = challengedProfilePicCount;
    }
    public Voting getVoting() {
        return mVoting;
    }

    public void setVoting(Voting voting) {
        mVoting = voting;
    }

    public Integer getLikeCount() {
        return mLikeCount;
    }

    public void setLikeCount(int likeCount) {
        mLikeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return mDislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        mDislikeCount = dislikeCount;
    }

    public Integer getCommentCount() {
        return mCommentCount;
    }

    public void setCommentCount(int commentCount) {
        mCommentCount = commentCount;
    }

    public String getSignedThumbnailUrl() {
        return mSignedThumbnailUrl;
    }

    public void setSignedThumbnailUrl(String signedThumbnailUrl) {
        mSignedThumbnailUrl = signedThumbnailUrl;
    }

    public String getChallengedTimeSinceStatus(Context context)
    {
        return Video.getTimeSince(context, mChallengedTime);
    }

    public Integer getChallengerProfilePicCount() {
        return mChallengerProfilePicCount;
    }

    public void setChallengerProfilePicCount(Integer challengerProfilePicCount) {
        mChallengerProfilePicCount = challengerProfilePicCount;
    }

    public String getChallengerProfilePicSignedUrl() {
        return mChallengerProfilePicSignedUrl;
    }

    public void setChallengerProfilePicSignedUrl(String challengerProfilePicSignedUrl) {
        mChallengerProfilePicSignedUrl = challengerProfilePicSignedUrl;
    }

    public Date getChallengedTime() {
        return mChallengedTime;
    }

    public void setChallengedTime(Date challengedTime) {
        mChallengedTime = challengedTime;
    }


    public enum Who_turn
    {
        YOUR_TURN,
        OPPONENT_TURN
    }
    //protected ArrayList<String> mSelectedJudgesFacebookIDList;



	public interface SignedUrlCallback
	{
	    void onReceivedSignedUrl(String signedUrl);
	}



    public String getOrientationLock() {
        return mOrientationLock;
    }

    public void setOrientationLock(String orientationLock) {
        mOrientationLock = orientationLock;
    }

    public String getChallengerName() {
        return mChallengerName;
    }

    public void setChallengerName(String challengerName) {
        mChallengerName = challengerName;
    }

    public String getChallengedName() {
        return mChallengedName;
    }

    public void setChallengedName(String challengedName) {
        mChallengedName = challengedName;
    }

    public String getChallengerUsername() {return mChallengerUsername;}
    public String getChallengedUsername() {return mChallengedUsername;}

    public void setRounds(int rounds) {
        mRounds = rounds;
    }

    public String getChallengedCognitoId() {
        return mChallengedCognitoId;
    }

    public void setChallengedCognitoId(String challengedCognitoId) {
        mChallengedCognitoId = challengedCognitoId;
    }

    public void setChallengerUsername(String challengerUsername) {
        mChallengerUsername = challengerUsername;
    }

    public void setChallengedUsername(String challengedUsername) {
        mChallengedUsername = challengedUsername;
    }

    public String getOpponentUsername() {
        return mOpponentUsername;
    }

    public void setOpponentUsername(String opponentUsername) {
        mOpponentUsername = opponentUsername;
    }


    public String getCurrentBattleStatus(Context context)
    {
        //get last uploaded videos time upload difference
        String currentBattleStatus = "";
        //if your turn
        if (mWhoTurn == Who_turn.YOUR_TURN)
        {
            currentBattleStatus = context.getResources().getString(R.string.your_turn);
        }
        else if ((mWhoTurn == Who_turn.OPPONENT_TURN))
        {
            currentBattleStatus = context.getResources().getString(R.string.opponent_turn);
        }
        return currentBattleStatus;
    }

    public String getTimeSinceLastVideosUploaded(Context context)
    {
        String timeSinceString;
        if (mVideosUploaded == 0)
        {
            timeSinceString = context.getResources().getString(R.string.battle_challenged_ago,Video.getTimeSince(context, mChallengedTime) );
        }
        else {
            timeSinceString = context.getResources().getString(R.string.last_video_updated_time_ago,Video.getTimeSince(context, mLastVideoUploadTime) );
        }
        return  timeSinceString;
    }

    public String getBattleStatus(Context context) {
        if (!mBattleAccepted) {
            return context.getString(R.string.declined_battle_status_message, mChallengedUsername);
        } else if (isBattleDone() && !mIsFinalVideoReady) {
            return context.getString(R.string.final_video_transcoding_status_message);
        } else if (isBattleDone() && mIsFinalVideoReady) {
            return getCompletedBattleStatus(context);
        } else return "";
    }

    public Integer getOpponentProfilePicCount(String cogntitoIDcurrent)
    {
        if (mChallengerCognitoId.equals(cogntitoIDcurrent))
        {
            return mChallengedProfilePicCount;
        }
        else if (mChallengedCognitoId.equals(cogntitoIDcurrent))
        {
            return mChallengerProfilePicCount;
        }
        else return 0;
    }

    public Integer getOpponentProfilePicCount()
    {
        String cognitoIDCurrent = IdentityManager.getDefaultIdentityManager().getCachedUserID();
        if (mChallengerCognitoId.equals(cognitoIDCurrent))
        {
            return mChallengedProfilePicCount;
        }
        else if (mChallengedCognitoId.equals(cognitoIDCurrent))
        {
            return mChallengerProfilePicCount;
        }
        else return 0;
    }

    public Who_turn getWhoTurn() {
        return mWhoTurn;
    }

    public void setWhoTurn(Who_turn whoTurn) {
        mWhoTurn = whoTurn;
    }




    public void setVideosUploaded(Integer videosUploaded) {
        mVideosUploaded = videosUploaded;
    }

    public void setVideos(ArrayList<Video> videos) {
        mVideos = videos;
    }

    public Integer getBattleID() {
        return mBattleID;
    }

    public void setBattleID(int battleID) {
        mBattleID = battleID;
    }

    public void setChallengerFacebookUserId(String challengerFacebookUserId) {
        mChallengerFacebookUserId = challengerFacebookUserId;
    }

    public void setChallengedFacebookUserId(String challengedFacebookUserId) {
        mChallengedFacebookUserId = challengedFacebookUserId;
    }
    public String getChallengerCognitoId() {
        return mChallengerCognitoId;
    }

    public void setChallengerCognitoId(String challengerCognitoId) {
        mChallengerCognitoId = challengerCognitoId;
    }
    public void setChallengedFacebookID(String challengedFacebookID)
    {
        mChallengedFacebookUserId = challengedFacebookID;
    }
    public void setBattleAccepted(boolean battleAccepted)
    {
        mBattleAccepted = battleAccepted;
    }
    public void setBattleName(String battleName)
    {
        mBattleName = battleName;
    }

    public Date getLastVideoUploadTime()
	 {
		 return mLastVideoUploadTime;
	 }

    public void setLastVideoUploadTime(Date lastVideoUploadTime) {
        mLastVideoUploadTime = lastVideoUploadTime;
    }


    public String getProfilePicSmallSignedUrl() {
        return mProfilePicSmallSignedUrl;
    }

    public void setProfilePicSmallSignedUrl(String profilePicSmallSignedUrl) {
        mProfilePicSmallSignedUrl = profilePicSmallSignedUrl;
    }

    public Integer getVideosUploaded() {
		return mVideosUploaded;
	}
	private String getThumbnailFilename()
	{
		return mBattleID + "-thumb.jpg";
	}
	public String getThumbnailServerUrl()
	{
		String imageURL = getChallengerCognitoID() + "/public/" + getThumbnailFilename();
		imageURL =  "https://djbj27vmux1mw.cloudfront.net/" + imageURL;
		return imageURL;
	}



	public Integer getBattleId()
	{
		return mBattleID;
	}
	public String getChallengedFacebookUserId() {
		return mChallengedFacebookUserId;
	}

	public String getOpponentName(String currentCognitoId)
	{
    		if (mChallengedCognitoId.equals(currentCognitoId))
			{
				return mChallengerUsername;
			}
			else if (mChallengerCognitoId.equals(currentCognitoId))
			{
				return mChallengedUsername;
			}
			else
			{
				return "";
			}
	}




	public String getChallengerFacebookUserId() {
		return mChallengerFacebookUserId;
	}
	public String getBattleName()
	{
		return mBattleName;
	}
	public Boolean getBattleAccepted() {
		return mBattleAccepted;
	}
	public String getChallengedCognitoID()
	{
		return mChallengedCognitoId;
	}
	public String getChallengerCognitoID()
	{
		return mChallengerCognitoId;
	}
	public String getOpponentCognitoID(String currentCognitoId)
	{
			if (mChallengedCognitoId.equals(currentCognitoId))
			{
				return mChallengerCognitoId;
			}
			else if (mChallengerCognitoId.equals(currentCognitoId))
			{
				return mChallengedCognitoId;
			}
			else
			{
				return "";
			}
	}


	public Integer getRounds() {
		return mRounds;
	}
	public ArrayList<Video> getVideos() {
		return mVideos;
	}
	public void addVideo(Video video)
	{
		mVideos.set(mVideosUploaded, video);
		mVideosUploaded++;
	}
	public boolean isBattleDone()
	{
		return mVideosUploaded == (mRounds * 2);
	}




	public String getFinalVideoFilename()
	{
		return mBattleID + "_final.mp4";
	}

    private static String getFinalVideoFilename(int battleID)
    {
        return battleID + "_final.mp4";
    }


	public String getServerFinalVideoUrl(String OwnerCognitoID)
	{
		return "https://d1lga8qqbgg58n.cloudfront.net/" + OwnerCognitoID + "/public/"  + getFinalVideoFilename();
	}

    public static String getServerFinalVideoUrlStatic(String OwnerCognitoID, int battleId)
    {
        return "https://d1lga8qqbgg58n.cloudfront.net/" + OwnerCognitoID + "/public/"  + getFinalVideoFilename(battleId);
    }

	public static void getSignedUrlFromServer(String finalVideoUrl, Context context, final SignedUrlCallback callback)
	{
		// Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
		LambdaInvokerFactory factory = new LambdaInvokerFactory(

				context.getApplicationContext(),
				Regions.US_EAST_1,
                IdentityManager.getDefaultIdentityManager().getCredentialsProvider());

		// Create the Lambda proxy object with default Json data binder.
		// You can provide your own data binder by implementing
		// LambdaDataBinder
		final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
		UrlLambdaRequest request = new UrlLambdaRequest();
        request.setUrl(finalVideoUrl);

		// The Lambda function invocation results in a network call
		// Make sure it is not called from the main thread
		new AsyncTask<UrlLambdaRequest, Void, String>() {
			@Override
			protected String doInBackground(UrlLambdaRequest... params) {
				// invoke "getSignedUrl" method. In case it fails, it will throw a
				// LambdaFunctionException.
				try {
					return lambdaFunctionsInterface.getSignedUrl(params[0]);
				} catch (LambdaFunctionException lfe) {
                    Timber.e(lfe, "Failed to invoke echo");
					return null;
				}
                catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Timber.i(ase.getErrorMessage());
                    return null;
                } catch (AmazonClientException ace) {
                    // Network issue
                    Timber.i(ace.toString());
                    return null;
                }
			}

			@Override
			protected void onPostExecute(String result) {
				if (result == null) {
					return;
				}
				callback.onReceivedSignedUrl(result);
			}
		}.execute(request);


	}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Battle battle = (Battle) o;
        return mLikeCount == battle.mLikeCount &&
                mDislikeCount == battle.mDislikeCount &&
                mDeleted == battle.mDeleted &&
                mIsFinalVideoReady == battle.mIsFinalVideoReady &&
                mCommentCount == battle.mCommentCount &&
                mBattleID.equals(battle.mBattleID) &&
                Objects.equals(mBattleName, battle.mBattleName) &&
                Objects.equals(mChallengerCognitoId, battle.mChallengerCognitoId) &&
                Objects.equals(mChallengedCognitoId, battle.mChallengedCognitoId) &&
                Objects.equals(mRounds, battle.mRounds) &&
                Objects.equals(mChallengerFacebookUserId, battle.mChallengerFacebookUserId) &&
                Objects.equals(mChallengedFacebookUserId, battle.mChallengedFacebookUserId) &&
                Objects.equals(mBattleAccepted, battle.mBattleAccepted) &&
                Objects.equals(mChallengerUsername, battle.mChallengerUsername) &&
                Objects.equals(mChallengedUsername, battle.mChallengedUsername) &&
                Objects.equals(mOpponentUsername, battle.mOpponentUsername) &&
                Objects.equals(mChallengerName, battle.mChallengerName) &&
                Objects.equals(mChallengedName, battle.mChallengedName) &&
                Objects.equals(mLastVideoUploadTime, battle.mLastVideoUploadTime) &&
                Objects.equals(mVideosUploaded, battle.mVideosUploaded) &&
                Objects.equals(mVideos, battle.mVideos) &&
                Objects.equals(mVideoViewCount, battle.mVideoViewCount) &&
                Objects.equals(mVoting, battle.mVoting) &&
                mWhoTurn == battle.mWhoTurn &&
                Objects.equals(mChallengedTime, battle.mChallengedTime) &&
                Objects.equals(mProfilePicSmallSignedUrl, battle.mProfilePicSmallSignedUrl) &&
                Objects.equals(mChallengerProfilePicCount, battle.mChallengerProfilePicCount) &&
                Objects.equals(mChallengedProfilePicCount, battle.mChallengedProfilePicCount) &&
                Objects.equals(mChallengerProfilePicSignedUrl, battle.mChallengerProfilePicSignedUrl) &&
                Objects.equals(mOrientationLock, battle.mOrientationLock) &&
                Objects.equals(mSignedThumbnailUrl, battle.mSignedThumbnailUrl) &&
                Objects.equals(mUserHasVoted, battle.mUserHasVoted) &&
                Objects.equals(mIsFollowingFeedBattle, battle.mIsFollowingFeedBattle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBattleID, mBattleName, mChallengerCognitoId, mChallengedCognitoId, mRounds, mChallengerFacebookUserId, mChallengedFacebookUserId, mBattleAccepted, mChallengerUsername, mChallengedUsername, mOpponentUsername, mChallengerName, mChallengedName, mLastVideoUploadTime, mVideosUploaded, mVideos, mVideoViewCount, mVoting, mLikeCount, mDislikeCount, mWhoTurn, mChallengedTime, mProfilePicSmallSignedUrl, mChallengerProfilePicCount, mChallengedProfilePicCount, mChallengerProfilePicSignedUrl, mOrientationLock, mDeleted, mIsFinalVideoReady, mCommentCount, mSignedThumbnailUrl, mUserHasVoted, mIsFollowingFeedBattle);
    }
}


