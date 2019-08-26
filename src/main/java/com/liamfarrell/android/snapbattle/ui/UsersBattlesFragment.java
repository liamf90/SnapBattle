package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.caches.OtherUsersProfilePicCacheManager;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;
import com.liamfarrell.android.snapbattle.model.User;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.deserializers.CustomLambdaDataBinder;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.GetUsersBattlesRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.GetUsersBattlesResponse;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;

import java.lang.ref.WeakReference;

public class UsersBattlesFragment extends BattleCompletedListFragment
{
    private static final String TAG  = "UsersBattlesFragment";
    private String mCognitoID;
    private boolean mIsFollowing;
    private User mUser;

    @Override
    protected int getLayoutID()
    {
        return R.layout.fragment_user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mCognitoID = getActivity().getIntent().getStringExtra(UsersBattlesActivity.EXTRA_COGNITO_ID);
        Log.i(TAG, "Cognito ID: " + mCognitoID);
    }



    @Override
    protected void getBattles() {
        GetUsersBattlesRequest request = new GetUsersBattlesRequest();
        request.setCognitoIDUser(mCognitoID);
        request.setFetchLimit(BATTLES_PER_FETCH);
        if (mBattles.size() > 0)
        {
            request.setGetAfterBattleID(mBattles.get(mBattles.size() - 1).getBattleId());
        }
        else
        {
            request.setGetAfterBattleID(-1);
        }
        new GetBattlesTask(getActivity(), this).execute(request);
    }

    @Override
    protected View.OnClickListener getBattleOnClickListener(final Battle b)
    {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filepath = b.getServerFinalVideoUrl(b.getChallengerCognitoID());
                Intent intent = new Intent(getActivity(), FullBattleVideoPlayerActivity.class);
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_BATTLEID, b.getBattleId());
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH, filepath);
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME, b.getChallengerUsername());
                intent.putExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME, b.getChallengedUsername());
                Log.i(TAG, "Orientation Lock: " + b.getOrientationLock());
                startActivityForResult(intent, FullBattleVideoPlayerFragment.VOTE_DONE_REQUEST_CODE);
            }
        };

    }

    private static class GetBattlesTask extends AsyncTask<GetUsersBattlesRequest, Void, AsyncTaskResult<GetUsersBattlesResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<UsersBattlesFragment> fragmentReference;

        GetBattlesTask(Activity activity, UsersBattlesFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<GetUsersBattlesResponse> doInBackground(GetUsersBattlesRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                FacebookLoginFragment.getCredentialsProvider(activityReference.get()));

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class, new CustomLambdaDataBinder());

                try {
                    GetUsersBattlesResponse response = lambdaFunctionsInterface.GetUsersBattles(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);                }
                catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                }
                catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(final AsyncTaskResult<GetUsersBattlesResponse> asyncResult) {
                // get a reference to the activity and fragment if it is still there
                UsersBattlesFragment fragment = fragmentReference.get();
                final Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                GetUsersBattlesResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {

                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;

                }

                int oldLastIndex =  fragment.mBattles.size();
                Log.i(TAG, "users topBattles received");
                for (Battle bat: result.getUser_battles())
                {
                    //Log.i(TAG, "Add BattlePOJO: " + bat.battleid);
                    if (!bat.isDeleted()) {
                        fragment.mBattles.add(bat);
                    }
                }
                fragment.mCurrentBattleAdapter.notifyItemRangeInserted(oldLastIndex,  fragment.mBattles.size());
                if (result.getUser_battles().size() != BATTLES_PER_FETCH)
                {
                    fragment.allBattlesFetched = true;
                }

                User user =result.getUser_profile();
                ((UsersBattlesActivity) activity).setToolbarText(user.getUsername());
                ((UsersBattlesActivity) activity).setBattlesCount(user.getBattleCount());
                ((UsersBattlesActivity) activity).setFollowersCount(user.getFollowingCount());
                ((UsersBattlesActivity) activity).setName(user.getFacebookName());
                ((UsersBattlesActivity) activity).setCognitoId(user.getCognitoId());

                //check if user is following
                fragment.mIsFollowing = user.getIsFollowing();
                Log.i(TAG, "Is Following: " + user.getIsFollowing());
                ((UsersBattlesActivity) activity).setIsFollowing(fragment.mIsFollowing);
                ((UsersBattlesActivity) activity).setIsFollowing(fragment.mIsFollowing);

                //get the profile pic for this user
                OtherUsersProfilePicCacheManager otherUsersProfilePicCacheManager = OtherUsersProfilePicCacheManager.getProfilePicCache(activity);
                otherUsersProfilePicCacheManager.getSignedUrlProfilePicOpponent(fragment.mCognitoID, user.getProfilePicCount(), user.getProfilePicSignedUrl(), activity, new OtherUsersProfilePicCacheManager.SignedUrlCallback() {
                    @Override
                    public void onSignedPicReceived(String signedUrl) {
                        ((UsersBattlesActivity)activity).setProfilePicture(signedUrl);
                    }
                });

                fragment.mProgressContainer.setVisibility(View.INVISIBLE);
            }
    }





}
