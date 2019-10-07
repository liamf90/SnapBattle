package com.liamfarrell.android.snapbattle.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.liamfarrell.android.snapbattle.HideAndShowBottomNavigation;
import com.liamfarrell.android.snapbattle.MainActivity;
import com.liamfarrell.android.snapbattle.caches.AllBattlesFeedCache;
import com.liamfarrell.android.snapbattle.caches.FollowingBattleCache;
import com.liamfarrell.android.snapbattle.di.Injectable;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.AddDislikeRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.DoVoteRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.FriendBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.LambdaFunctionsInterface;
import com.liamfarrell.android.snapbattle.R;
import com.liamfarrell.android.snapbattle.model.Voting;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.pjos.FollowingBattleVideoViewPOJO;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.IncreaseVideoViewCountRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.request.ReportBattleRequest;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DefaultResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.DoVoteResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.FriendBattleResponse;
import com.liamfarrell.android.snapbattle.model.aws_lambda_function_deserialization.aws_lambda_functions.response.ReportBattleResponse;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.AddLikeRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UndoDislikeRequest;
import com.liamfarrell.android.snapbattle.model.lambda_function_request_objects.UndoLikeRequest;
import com.liamfarrell.android.snapbattle.mvvm_ui.ViewCommentsFragment;
import com.liamfarrell.android.snapbattle.util.HandleLambdaError;
import com.liamfarrell.android.snapbattle.views.VideoControllerView;
import com.liamfarrell.android.snapbattle.mvvm_ui.create_battle.ChooseVotingFragment;
import com.liamfarrell.android.snapbattle.model.AsyncTaskResult;
import com.liamfarrell.android.snapbattle.model.Battle;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by Liam on 20/01/2018.
 *
 * Needs to be updated to kotlin with mvvm architecture
 */

public class FullBattleVideoPlayerFragment extends VideoPlayerAbstractFragment implements Injectable {

    private String filepath;
    private int mBattleID;

    private boolean isLiked;
    private boolean isDisliked;
    private int dislikeCount;

    private int likeCount;
    //private boolean isPaused = false;
    //private int pausedPosition = 0;
    private String mChallengerName = "Challenger";
    private String mChallengedName = "Challenged";

    private static AWSCredentialsProvider credentialsProvider = AWSMobileClient.getInstance().getCredentialsProvider();

    public static final String EXTRA_BATTLEID = "com.liamfarrell.android.snapbattle.videoplayeractivity.battleIDextra";

    public static final int VOTE_DONE_REQUEST_CODE = 555;
    public static final String INTENT_EXTRA_BATTLE_ID_VOTE_DONE_REQUEST = "com.liamfarrell.android.snapbattle.fullbattlevideoplayer.battleidrequestextra";



    public static final String TAG = "FullBattle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //filepath = getActivity().getIntent().getStringExtra(FullBattleVideoPlayerActivity.EXTRA_FILE_VIDEO_PATH);
        filepath = getArguments().getString("filepath");
        mChallengerName = getArguments().getString("challengerName");
        mChallengedName = getArguments().getString("challengedName");
        Timber.i("Filepath: " + filepath);
        //mBattleID = getActivity().getIntent().getIntExtra(EXTRA_BATTLEID, 0);
        mBattleID = getArguments().getInt("battleId");
        isLiked = false;
        isDisliked = false;
//        mChallengerName = getActivity().getIntent().getStringExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGER_USERNAME);
//        mChallengedName = getActivity().getIntent().getStringExtra(FullBattleVideoPlayerActivity.EXTRA_CHALLENGED_USERNAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        ((HideAndShowBottomNavigation)getActivity()).hideBottomNavigation();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((HideAndShowBottomNavigation)getActivity()).showBottomNavigation();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    private void checkCanVote(final VideoControllerView controller, int hasUserVoted, ChooseVotingFragment.VotingChoice votingChoice, Date votingTimeEnd, final String facebookUserIdChallenger, final String facebookUserIdChallenged, String currentUserCognitoId, String challengerCognitoId, String challengedCognitoId)
    {
        Timber.i("User has voted: " + hasUserVoted);

        //check current user is not trying to vote on own battle
        if (currentUserCognitoId.equals(challengerCognitoId) || currentUserCognitoId.equals(challengedCognitoId))
        {
            controller.setVotingButtonInvisible();
            return;
        }

        if (hasUserVoted == 1)
        {
            controller.setVoteButtonVisible();
            controller.setVoteButtonVoted();
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date timeNow = cal.getTime();
            if (timeNow.after(votingTimeEnd))
            {
                controller.setVotingButtonInvisible();
                return;
            }


            if (votingChoice.equals(ChooseVotingFragment.VotingChoice.PUBLIC))
            {
                Log.i(TAG, "Setting vote button public");
                controller.setVoteButtonVisible();
            }
            else if (votingChoice.equals(ChooseVotingFragment.VotingChoice.NONE))
            {
                controller.setVotingButtonInvisible();
            }
            else if (votingChoice.equals(ChooseVotingFragment.VotingChoice.MUTUAL_FACEBOOK))
            {
                Log.i(TAG, "Making the API call to get mutual facebook friends");
            /* make the API call */
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/friends/" + facebookUserIdChallenger,
                        null,
                        com.facebook.HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response)
                            {

                                Log.i(TAG,  response.toString());
                                //if the users are not facebook mutual friends a JSONException will be thrown as we cannot get the JSON Object
                                //check both challenger and challenged of the battle, if code gets through to setVoteButtonEnabled that means the user is
                                //facebook friends with both

                                JSONObject friend = null;
                                try {
                                    friend = response.getJSONObject().getJSONArray("data").getJSONObject(0);
                                    new GraphRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            "/me/friends/" + facebookUserIdChallenged,
                                            null,
                                            com.facebook.HttpMethod.GET,
                                            new GraphRequest.Callback() {
                                                @Override
                                                public void onCompleted(GraphResponse response)
                                                {

                                                    Log.i(TAG,  response.toString());
                                                    JSONObject friend = null;
                                                    try {
                                                        friend = response.getJSONObject().getJSONArray("data").getJSONObject(0);
                                                        controller.setVoteButtonVisible();

                                                    } catch (JSONException e) {
                                                    }



                                                }
                                            }
                                    ).executeAsync();
                                } catch (JSONException e) {
                                    controller.setVotingButtonInvisible();

                                }



                            }
                        }
                ).executeAsync();


            }
        }

    }



    @Override
    protected void setVideoFilepath()
    {

        Log.i(TAG, "Setting video Filepath");
        Battle.SignedUrlCallback callback = new Battle.SignedUrlCallback() {
        @Override
        public void onReceivedSignedUrl(String signedUrl)
        {
            Log.i("FullBattleFragment", "Signed Url: " + signedUrl);
            //mVideoView.setVideoPath(signedUrl);

            mVideoView.setVideoURI( Uri.parse(signedUrl));
            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.i(TAG, "ON ERROR: what= " + what + "extra: " + extra);

                    //if the signed url has expired. reset the video filepath. else exit callbacks
                    if (what == 1) {
                        setVideoFilepath();
                    }
                    else {
                        getActivity().finish();
                    }
                    return true;
                }
            });
            if (isPaused) {
                //mMediaPlayer.seekTo(pausedPosition);
                mVideoView.pause();

            }
            else{
                mVideoView.start();
            }




        }
    };
        Battle.getSignedUrlFromServer(filepath, getActivity(), callback);

        getBattle();

    }


    @Override
    protected void setVideoController(View v)
    {


        mVideoController = new VideoControllerView(getActivity());

        mVideoController.setAnchorView((FrameLayout) v.findViewById(R.id.videoContainer));
        ((VideoControllerView) mVideoController).setCommentsCallback(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isPaused = true;
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                }
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction().add(R.id.nav_host_container, new ViewCommentsFragment()).addToBackStack(null).commit();
                NavDirections directions = FullBattleVideoPlayerFragmentDirections.Companion.actionFullBattleVideoPlayerFragmentToViewCommentsFragment(mBattleID);
                Navigation.findNavController(view).navigate(directions);
                //((FullBattleVideoPlayerActivity)getActivity()).startCommentsFragment();
            }
        });

        ((VideoControllerView) mVideoController).makeLikeDislikeButtonsNotEnabled();
        ((VideoControllerView) mVideoController).setVoteButtonCallback(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                CharSequence[] items = new CharSequence[2];
                items[0] = mChallengerName;
                items[1] = mChallengedName;
                //items[0] = "challenger";
                //items[1] = "challenged";


                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setCancelable(true).setTitle(R.string.vote_dialog_title).setItems(items,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0)
                                {
                                    doVote(Voting.ChallengerOrChallenged.CHALLENGER);
                                }
                                else if (i ==1)
                                {
                                    doVote(Voting.ChallengerOrChallenged.CHALLENGED);
                                }
                                dialogInterface.dismiss();

                            }
                        });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        ((VideoControllerView) mVideoController).setLikeCallback(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isLiked) {
                    Log.i("VideoPlayer", "Un-Liking");
                    ((VideoControllerView) mVideoController).setLikesCount(--likeCount);
                    isLiked = false;
                    ((VideoControllerView) mVideoController).setLikeButtonUnLiked();
                    undoLike();
                }
                else
                {
                    if (isDisliked)
                    {
                        isDisliked = false;
                        ((VideoControllerView) mVideoController).setDislikesCount(--dislikeCount);
                        ((VideoControllerView) mVideoController).setDisLikeButtonUndisliked();
                    }
                    ((VideoControllerView) mVideoController).setLikesCount(++likeCount);
                    Log.i("VideoPlayer", "Liking");
                    isLiked = true;
                    ((VideoControllerView) mVideoController).setLikeButtonLiked();
                    likeBattle();
                }


            }
        });
        ((VideoControllerView) mVideoController).setDislikeCallback(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDisliked) {
                    Log.i(TAG, "Un-Disliking");
                    isDisliked = false;
                    ((VideoControllerView) mVideoController).setDislikesCount(--dislikeCount);
                    ((VideoControllerView) mVideoController).setDisLikeButtonUndisliked();
                    undoDislikeBattle();


                }
                else
                {
                    if (isLiked)
                    {
                        isLiked = false;
                        ((VideoControllerView) mVideoController).setLikesCount(--likeCount);
                        ((VideoControllerView) mVideoController).setLikeButtonUnLiked();
                    }
                    ((VideoControllerView) mVideoController).setDislikesCount(++dislikeCount);
                    isDisliked = true;
                    Log.i(TAG, "Disliking");
                    ((VideoControllerView) mVideoController).setDisLikeButton();
                    dislikeBattle();

                }




            }
        });

        ((VideoControllerView) mVideoController).setReportButtonCallback(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportBattle();
            }
        });
        super.setUpVideoViewCountIncreaser(new ViewCountCallback() {
            @Override
            public void onViewCountIncrease() {
                addVideoView();
            }
        });
    }

    private void reportBattle() {
        ((VideoControllerView) mVideoController).setReportButtonReporting();
        ReportBattleRequest reportBattleRequest = new ReportBattleRequest();
        reportBattleRequest.setBattleID(mBattleID);
        new ReportBattleTask(getActivity(), this).execute(reportBattleRequest);
    }

    private static class ReportBattleTask extends AsyncTask<ReportBattleRequest, Void,AsyncTaskResult<ReportBattleResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        ReportBattleTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<ReportBattleResponse> doInBackground(ReportBattleRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                   ReportBattleResponse response = lambdaFunctionsInterface.ReportBattle(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR", lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                } catch (AmazonServiceException ase) {
                    // invalid credentials, incorrect AWS signature, etc
                    Log.i("ERROR", ase.getErrorMessage());
                    return new AsyncTaskResult<>(ase);
                } catch (AmazonClientException ace) {
                    // Network issue
                    Log.i("ERROR", ace.toString());
                    return new AsyncTaskResult<>(ace);
                }
            }

            @Override
            protected void onPostExecute(AsyncTaskResult<ReportBattleResponse> asyncResult) {
                // get a reference to the callbacks if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                ReportBattleResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;
                }

                    ((VideoControllerView) fragment.mVideoController).setReportButtonReported();
                    if (result.getAffectedRows() == 1)
                    {


                    }
                    else
                    {
                        Toast.makeText(activity, R.string.error_already_reported, Toast.LENGTH_LONG).show();
                    }
            }


    }




    private void likeBattle() {
        AddLikeRequest request = new AddLikeRequest();
        request.setBattleid(mBattleID);
        new LikeBattleTask(getActivity(), this).execute(request);
    }

    private static class LikeBattleTask extends AsyncTask<AddLikeRequest, Void,  AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        LikeBattleTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected  AsyncTaskResult<DefaultResponse> doInBackground(AddLikeRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response = lambdaFunctionsInterface.AddLike(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                // get a reference to the callbacks if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                DefaultResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;
                }


        }
    }

    private void dislikeBattle() {
        AddDislikeRequest request = new AddDislikeRequest();
        request.setBattleid(mBattleID);
        new DislikeBattleTask(getActivity(), this).execute(request);
    }

    private static class DislikeBattleTask extends  AsyncTask<AddDislikeRequest, Void,  AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        DislikeBattleTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        protected  AsyncTaskResult<DefaultResponse> doInBackground(AddDislikeRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);

        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread

                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response =   lambdaFunctionsInterface.AddDisLike(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                DefaultResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;

                }
            }
    }

    private void doVote(Voting.ChallengerOrChallenged challengerOrChallenged) {
        ((VideoControllerView)mVideoController).setVoteButtonDisabled();
        DoVoteRequest request = new DoVoteRequest();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        String token = accessToken.getToken();
        request.setBattleID(mBattleID);
        request.setFacebookToken(token);
        request.setChallengerOrChallenged(challengerOrChallenged);
        new DoVoteTask(getActivity(),this).execute(request);

    }

    private static class DoVoteTask extends AsyncTask<DoVoteRequest, Void, AsyncTaskResult<DoVoteResponse>> {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        DoVoteTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<DoVoteResponse> doInBackground(DoVoteRequest... params)
        {


        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DoVoteResponse response =  lambdaFunctionsInterface.doVote(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DoVoteResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();

                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;
                Context appContext = activity.getApplicationContext();

                DoVoteResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;

                }
                ((VideoControllerView) fragment.mVideoController).setVoteButtonVoted();
                //TODO fix this below
//                AllBattlesFeedCache.get(appContext).updateUserHasVoted(appContext,result.getBattleID() );
//                FollowingBattleCache.get(appContext).updateUserHasVoted(appContext,result.getBattleID() );
                Intent i = new Intent();
                i.putExtra(INTENT_EXTRA_BATTLE_ID_VOTE_DONE_REQUEST, result.getBattleID());
                activity.setResult(Activity.RESULT_OK, i);
            }
    }

    private void undoLike() {
        UndoLikeRequest request = new UndoLikeRequest();
        request.setBattleid(mBattleID);
        new UndoLikeTask(getActivity(), this).execute(request);
    }
    private static class UndoLikeTask extends  AsyncTask<UndoLikeRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        UndoLikeTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }

        @Override
        protected AsyncTaskResult<DefaultResponse> doInBackground(UndoLikeRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread

                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response = lambdaFunctionsInterface.UndoLike(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                // get a reference to the callbacks and fragment if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                DefaultResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                }

            }
    }


    private void addVideoView() {
        IncreaseVideoViewCountRequest request = new IncreaseVideoViewCountRequest();
        request.setBattleID(mBattleID);
        new AddVideoViewTask(getActivity(), this).execute(request);
    }

    private static class AddVideoViewTask extends AsyncTask<IncreaseVideoViewCountRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        AddVideoViewTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<DefaultResponse> doInBackground(IncreaseVideoViewCountRequest... params) {
        Log.i(TAG, "Adding VideoPOJO View Count");
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response = lambdaFunctionsInterface.IncreaseViewViewCount(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {

            }
    }

    private void undoDislikeBattle() {
        UndoDislikeRequest request = new UndoDislikeRequest();
        request.setBattleid(mBattleID);
        new UndoDislikeBattleTask(getActivity()).execute(request);
    }

    private static class UndoDislikeBattleTask extends AsyncTask<UndoDislikeRequest, Void, AsyncTaskResult<DefaultResponse>>
    {
        private WeakReference<Activity> activityReference;

        UndoDislikeBattleTask(Activity activity)
        {
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<DefaultResponse> doInBackground(UndoDislikeRequest... params) {
        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        // Create the Lambda proxy object with default Json data binder.
        // You can provide your own data binder by implementing
        // LambdaDataBinder
        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);


        // The Lambda function invocation results in a network call
        // Make sure it is not called from the main thread


                // invoke "echo" method. In case it fails, it will throw a
                // LambdaFunctionException.
                try {
                    DefaultResponse response = lambdaFunctionsInterface.UndoDislike(params[0]);
                    return new AsyncTaskResult<>(response);
                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute(AsyncTaskResult<DefaultResponse> asyncResult) {
                // get a reference to the callbacks if it is still there
                Activity activity = activityReference.get();
                if (activity == null || activity.isFinishing()) return;

                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity,null);
                    return;
                }

            }
    }

    private void getBattle() {
        FriendBattleRequest request = new FriendBattleRequest();
        request.setBattleID(mBattleID);
        new GetBattleTask(getActivity(), this).execute(request);
    }

    private static class GetBattleTask extends AsyncTask<FriendBattleRequest, Void,AsyncTaskResult<FriendBattleResponse>>
    {
        private WeakReference<Activity> activityReference;
        private WeakReference<FullBattleVideoPlayerFragment> fragmentReference;

        GetBattleTask(Activity activity, FullBattleVideoPlayerFragment fragment)
        {
            fragmentReference = new WeakReference<>(fragment);
            activityReference = new WeakReference<>(activity);
        }
        @Override
        protected AsyncTaskResult<FriendBattleResponse> doInBackground(FriendBattleRequest... params) {

        // Create a LambdaInvokerFactory, to be used to instantiate the Lambda proxy
        LambdaInvokerFactory factory = new LambdaInvokerFactory(
                activityReference.get().getApplicationContext(),
                Regions.US_EAST_1,
                credentialsProvider);

        final LambdaFunctionsInterface lambdaFunctionsInterface = factory.build(LambdaFunctionsInterface.class);
                try {
                    FriendBattleResponse response = lambdaFunctionsInterface.GetFriendBattle(params[0]);
                    return new AsyncTaskResult<>(response);

                } catch (LambdaFunctionException lfe) {
                    Log.i("ERROR", lfe.getDetails());
                    Log.i("ERROR",lfe.getStackTrace().toString());
                    lfe.printStackTrace();

                    return new AsyncTaskResult<>(lfe);
                }
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
            protected void onPostExecute( AsyncTaskResult<FriendBattleResponse> asyncResult) {
            Log.i(TAG, "BattlePOJO received from server");
                // get a reference to the callbacks and fragment if it is still there
                FullBattleVideoPlayerFragment fragment = fragmentReference.get();
                Activity activity = activityReference.get();
                if (fragment == null || fragment.isRemoving()) return;
                if (activity == null || activity.isFinishing()) return;

                FriendBattleResponse result = asyncResult.getResult();
                if (asyncResult.getError() != null)
                {
                    new HandleLambdaError().handleError(asyncResult.getError(), activity, null);
                    return;


                }


                FollowingBattleVideoViewPOJO followingBattleVideoViewPOJO = result.getSql_result();
                if (followingBattleVideoViewPOJO.getDeleted() == 1)
                {
                    Toast.makeText(activity, R.string.error_battle_deleted, Toast.LENGTH_LONG).show();
                    activity.finish();
                }


                if (followingBattleVideoViewPOJO.getUser_has_disliked() == 1)
                {
                    fragment.isDisliked = true;
                    ((VideoControllerView) fragment.mVideoController).setDisLikeButton();
                }
                if (followingBattleVideoViewPOJO.getUser_has_liked() == 1)
                {
                    fragment.isLiked = true;
                    ((VideoControllerView) fragment.mVideoController).setLikeButtonLiked();
                }

                fragment.likeCount = followingBattleVideoViewPOJO.getLike_count();
                fragment.dislikeCount = followingBattleVideoViewPOJO.getDislike_count();

                ((VideoControllerView)fragment. mVideoController).setLikesCount(fragment.likeCount);
                ((VideoControllerView) fragment.mVideoController).setDislikesCount(fragment.dislikeCount);
                ((VideoControllerView) fragment.mVideoController).setCommentsButtonText(activity.getResources().getQuantityString(R.plurals.view_comments_button, followingBattleVideoViewPOJO.getComment_count(), followingBattleVideoViewPOJO.getComment_count()));
                ((VideoControllerView) fragment.mVideoController).makeLikeDislikeButtonsEnabled();
                //mChallengerName = result.sql_result.challengerFacebookName;
                //mChallengedName = result.sql_result.challengedFacebookName;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                if (followingBattleVideoViewPOJO.getVoting_time_end() != null) {
                    try {
                        Date votingTimeEnd = sdf.parse(followingBattleVideoViewPOJO.getVoting_time_end());
                        if (activityReference.get() != null) {
                            fragment.checkCanVote(((VideoControllerView) fragment.mVideoController), followingBattleVideoViewPOJO.getUser_has_voted(), ChooseVotingFragment.VotingChoice.valueOf(followingBattleVideoViewPOJO.getVoting_type()), votingTimeEnd, followingBattleVideoViewPOJO.getChallenger_facebook_id(), followingBattleVideoViewPOJO.getChallenged_facebook_id(),
                                   result.getUserRequestingCognitoId(), followingBattleVideoViewPOJO.getChallenger_cognito_id(), followingBattleVideoViewPOJO.getChallenged_cognito_id());
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }
    }





}